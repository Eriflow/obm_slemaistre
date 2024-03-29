package org.minig.imap.command.parser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.minig.imap.mime.MimePart;
import org.minig.imap.mime.MimeType;
import org.minig.imap.mime.impl.BodyParamParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BodyStructureParser {

	private final static Logger logger = LoggerFactory
			.getLogger(BodyStructureParser.class);
	
	@BuildParseTree
	static class Rules extends AbstractImapBaseParser {
	
		Rule addrAdl() {
			return nstringNoStack();
		}

		Rule address() {
			return Sequence('(', addrName(), whitespaces(), addrAdl(), whitespaces(), 
					addrMailbox(), whitespaces(), addrHost(), ')');

		}

		Rule addrHost() {
			return nstringNoStack();
		}

		Rule addrMailbox() {
			return nstringNoStack();
		}

		Rule addrName() {
			return nstringNoStack();
		}

		boolean addMimePart() {
			IMimePart obj = (IMimePart) pop();
			IMimePart mimeParent = (IMimePart) peek();
			mimeParent.addPart(obj);
			return true;
		}
				
		Rule body() {
			return Sequence('(', FirstOf(bodyType1part(), bodyTypeMPart()), ')');
		}

		boolean pushBodyParams() {
			swap();
			pop();
			Set<BodyParam> bodyParams = (Set<BodyParam>)pop();
			List<Object> list = (List<Object>)peek();
			list.add(bodyParams);
			return true;
		}
		
		Rule bodyExt1Part() {
			return Sequence(bodyFldMd5(), drop() && push(Lists.newArrayList()),
					Optional(whitespaces(), bodyFldDsp(), pushBodyParams(),
							Optional(whitespaces(), bodyFldLang(),
									Optional(whitespaces(), bodyFldLoc(),
											ZeroOrMore(whitespaces(), bodyExtension())))));
		}
		
		Rule bodyExtension() {
			return FirstOf(nstring(), drop(), number(), drop(), Sequence('(', bodyExtension(), ZeroOrMore(whitespaces(), bodyExtension()), ')'));
		}

		Rule bodyExtMPart() {
			return Sequence(bodyFldParam(), push(Lists.newArrayList(pop())),
					Optional(whitespaces(), bodyFldDsp(), drop() && drop(),
							Optional(whitespaces(), bodyFldLang(), 
									Optional(whitespaces(), bodyFldLoc(),
											ZeroOrMore(whitespaces(), bodyExtension())))));
		}

		Rule bodyFields() {
			return SequenceWithWhitespaces(
					bodyFldParam(), bodyFldId(), bodyFldDesc(), bodyFldEnc(), bodyFldOctets());
		}

		Rule bodyFldDesc() {
			return Sequence(nstring(), drop());
		}

		Rule bodyFldDsp() {
			return FirstOf(Sequence('(', string(), whitespaces(), bodyFldParam(), ')'), 
					Sequence(nil(), push(null)));
		}

		Rule bodyFldEnc() {
			return FirstOf(
					Sequence('"', 
							FirstOf("7BIT", "8BIT", "BINARY", "BASE64", "QUOTED-PRINTABLE"), push(match()), 
							'"'),
					string());
		}

		Rule bodyFldId() {
			return nstring();
		}

		Rule bodyFldLang() {
			return FirstOf(Sequence(nstring(), drop()), Sequence('(', string(), drop(), ZeroOrMore(whitespaces(), string(), drop()), ')'));
		}

		Rule bodyFldLines() {
			return Sequence(number(), drop());
		}

		Rule bodyFldLoc() {
			return Sequence(nstring(), drop());
		}

		Rule bodyFldMd5() {
			return nstring();
		}

		Rule bodyFldOctets() {
			return Sequence(number(), drop());
		}

		boolean addBodyParam() {
			swap();
			BodyParam bodyParam = BodyParamParser.parse((String)pop(), (String)pop());
			HashSet<BodyParam> bodyParams = (HashSet<BodyParam>)peek();
			bodyParams.add(bodyParam);
			return true;
		}
		
		Rule bodyFldParam() {

			return FirstOf(
					Sequence(
							push(new HashSet<BodyParam>()),
							'(', 
							string(),
							whitespaces(),
							string(),
							addBodyParam(),
							ZeroOrMore(
									whitespaces(), string(), whitespaces(), string(), 
									addBodyParam()), 
					')'),
					Sequence(nil(), drop() && push(ImmutableSet.of())));
		}

		Rule bodyType1part() {
			//be careful, bodyTypeBasic is an incompatible superset of bodyTypeText
			//so rules must be kept in this order
			return Sequence(FirstOf(bodyTypeText(), bodyTypeMsg(), bodyTypeBasic()),
					Optional(whitespaces(), bodyExt1Part(), mergeBodyParams()),
					addMimePart());
		}

		boolean mergeBodyParams() {
			List<Object> extParts = (List<Object>)pop();
			Set<BodyParam> extBodyParams = (Set<BodyParam>)extParts.get(0);
			IMimePart mt = (IMimePart) peek();

			if (extBodyParams != null) {
				Set<BodyParam> newParams = Sets.newHashSet();
				Collection<BodyParam> bodyParams = mt.getBodyParams();
				if (bodyParams != null) {
					newParams.addAll(bodyParams);
				}
				newParams.addAll(extBodyParams);
				mt.setBodyParams(newParams);
			}
			return true;
		}

		Rule bodyTypeBasic() {
			return Sequence(mediaBasic(), whitespaces(), bodyFields(), createMimePart());
		}

		Rule bodyTypeMPart() {
			return Sequence(push(new MimePart()), OneOrMore(body()), 
							whitespaces(), push("multipart"), mediaSubType(), 
							createMimeType(), setMimeType(),
					Optional(whitespaces(), bodyExtMPart(), mergeBodyParams()),
					addMimePart());
		}

		boolean setMimeType() {
			MimeType mimetype = (MimeType) pop();
			IMimePart mimePart = (IMimePart) peek();
			mimePart.setMimeType(mimetype);
			return true;
		}
		
		Rule bodyTypeMsg() {
			return Sequence(mediaMessage(), whitespaces(), bodyFields(), whitespaces(), envelope(), 
					createMimePart(),
					whitespaces(), body(), whitespaces(), bodyFldLines(), mergeMultipartWithMessage());
		}
		
		boolean mergeMultipartWithMessage() {
			MimePart message = (MimePart) peek();
			IMimePart child = message.getChildren().get(0);
			if (child.isMultipart()) {
				Collection<BodyParam> params = Lists.newArrayList(
						Iterables.concat(message.getBodyParams(), child.getBodyParams()));
				message.setBodyParams(params);
				message.setChildren(child.getChildren());
				message.setMultipartSubtype(child.getMimeSubtype());
			}
			return true;
		}
		
		Rule bodyTypeText() {
			return Sequence(mediaText(), whitespaces(), bodyFields(), whitespaces(), bodyFldLines(),
					createMimePart());
		}

		boolean createMimePart() {
			String contentTransfertEncoding = (String)pop();
			String bodyId = (String)pop();
			Set<BodyParam> bodyParams = (Set<BodyParam>) pop();
			MimeType mimeType = (MimeType) pop();
			MimePart mimePart = new MimePart();
			mimePart.setMimeType(mimeType);
			mimePart.setBodyParams(bodyParams);
			mimePart.setContentId(bodyId);
			mimePart.setContentTransfertEncoding(contentTransfertEncoding);
			push(mimePart);
			return true;
		}

		Rule envBcc() {
			return addressListNoStack();
		}

		Rule addressListNoStack() {
			return FirstOf(Sequence('(', OneOrMore(address()),')'), nilNoStack());
		}

		Rule envCc() {
			return addressListNoStack();
		}

		Rule envDate() {
			return nstringNoStack();
		}

		Rule envelope() {
			return Sequence('(', envDate(), whitespaces(), 
					envSubject(), whitespaces(),
					envFrom(), whitespaces(),
					envSender(), whitespaces(),
					envReplyTo(), whitespaces(),
					envTo(), whitespaces(),
					envCc(), whitespaces(),
					envBcc(), whitespaces(),
					envInReplyTo(), whitespaces(),
					envMessageId(), ')');
		}

		Rule envFrom() {
			return addressListNoStack();
		}

		Rule envInReplyTo() {
			return nstringNoStack();
		}

		Rule envMessageId() {
			return nstringNoStack();
		}

		Rule envReplyTo() {
			return addressListNoStack();
		}

		Rule envSender() {
			return addressListNoStack();
		}

		Rule envSubject() {
			return nstringNoStack();
		}

		Rule envTo() {
			return addressListNoStack();
		}

		Rule mediaBasic() {
			return Sequence(FirstOf(
						Sequence('"', FirstOf("APPLICATION", "AUDIO", "IMAGE", "MESSAGE", "VIDEO"), push(match()), '"'),
						string()),
					whitespaces(),
					mediaSubType(),
					createMimeType()
					);
		}

		boolean createMimeType() {
			swap();
			push(new MimeType((String)pop(), (String)pop()));
			return true;
		}
		
		Rule mediaMessage() {
			return Sequence("\"MESSAGE\"", whitespaces(), "\"RFC822\"", push(new MimeType("MESSAGE", "RFC822")));
		}

		Rule mediaSubType() {
			return string();
		}

		Rule mediaText() {
			return Sequence("\"TEXT\"", whitespaces(), mediaSubType(), push(new MimeType("TEXT", (String) pop())));
		}

		public Rule rule() {
			return Sequence(push(new MimePart()), body(), EOI, takeFirstMimePart());
		}

		boolean takeFirstMimePart() {
			IMimePart tree = (IMimePart) pop();
			IMimePart mimePart = tree.getChildren().get(0);
			mimePart.defineParent(null, 0);
			push(mimePart);
			return true;
		}
	}
	
	private static final Rules parser = Parboiled.createParser(BodyStructureParser.Rules.class);
	
	public MimeMessage parseBodyStructureDebug(String payload) {
		Rules parserInstance = parser.newInstance();
		TracingParseRunner<IMimePart> runner = new TracingParseRunner<IMimePart>(parserInstance.rule());
		try {
			ParsingResult<IMimePart> result = runner.run(payload);
			IMimePart mimeContainer = result.resultValue;
			MimeMessage mimeMessage = new MimeMessage(mimeContainer);
			return mimeMessage;
		} finally {
			logToFile(runner);
		}
	}


	private static void logToFile(TracingParseRunner<IMimePart> runner) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream("/tmp/log");
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
			outputStreamWriter.write(runner.getLog().toString());
			outputStreamWriter.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	
	public MimeMessage parseBodyStructure(String payload) {
		Rules parserInstance = parser.newInstance();
		RecoveringParseRunner<IMimePart> runner = 
			//new RecoveringParseRunner<MimeTree>(parserInstance.rule(), new DebugValueStack());
			new RecoveringParseRunner<IMimePart>(parserInstance.rule());

		ParsingResult<IMimePart> result = runner.run(payload);
		IMimePart mimeContainer = result.resultValue;
		MimeMessage mimeMessage = new MimeMessage(mimeContainer);
		return mimeMessage;

		//logger.info(ParseTreeUtils.printNodeTree(result));
	}
}

package org.obm.sync.server.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import org.obm.sync.server.mailer.AbstractMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.services.constant.ConstantService;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class TemplateLoaderFreeMarkerImpl implements ITemplateLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(CalendarBindingImpl.class);

	private ConstantService constantService;

	@Inject
	public TemplateLoaderFreeMarkerImpl(ConstantService constantService) {
		this.constantService = constantService;
	}

	private Configuration getDefaultCfg() {
		Configuration externalCfg = new Configuration();
		externalCfg
				.setClassForTemplateLoading(AbstractMailer.class, "template");
		return externalCfg;
	}

	private Configuration getOverrideCfg() throws IOException {
		Configuration externalCfg = new Configuration();
		externalCfg.setDirectoryForTemplateLoading(new File(constantService
				.getOverrideTemplateFolder()));
		return externalCfg;
	}

	public Template getTemplate(String templateName, Locale locale,
			TimeZone timezone) throws IOException {
		Template ret = null;
		try {
			ret = getOverrideCfg().getTemplate(templateName, locale);
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Error while loading Template[ " + templateName
								+ "] in "
								+ constantService.getOverrideTemplateFolder(),
						e);
			}
		}
		if (ret == null) {
			ret = getDefaultCfg().getTemplate(templateName, locale);
		}
		if (ret == null) {
			throw new FileNotFoundException("Error while loading Template[ "
					+ templateName + "] in "
					+ constantService.getDefaultTemplateFolder());
		}
		ret.setTimeZone(timezone);
		return ret;
	}
}

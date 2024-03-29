package org.obm.push.protocol.data;

import org.apache.commons.codec.binary.Base64;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEventUid;

public class EmailEncoderTest {

	private EmailEncoder emailEncoder;

	@Before
	public void setUp() {
		emailEncoder = new EmailEncoder(new IntEncoder());
	}
	
	@Test
	public void testConvertMSEventUidToGlobalObjId() {
		/*
		 * Bytes 1-16:  <04><00><00><00><82><00><E0><00><74><C5><B7><10><1A><82><E0><08>
		 * Bytes 17-20: <00><00><00><00>
		 * Bytes 21-36: <00><00><00><00><00><00><00><00><00><00><00><00><00><00><00><00>
		 * Bytes 37-40: <33><00><00><00>
		 * Bytes 41-52: vCal-Uid<01><00><00><00>
		 * Bytes 53-91: {81412D3C-2A24-4E9D-B20E-11F7BBE92799}<00>
		 */
		byte[] expectedBytes = emailEncoder.buildByteSequence(
				0x04, 0x00, 0x00, 0x00, 0x82, 0x00, 0xE0, 0x00, 0x74, 0xC5, 0xB7, 0x10, 
				0x1A, 0x82, 0xE0, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x33, 0x00, 0x00, 0x00, 0x76, 0x43, 0x61, 0x6C, 0x2D, 0x55, 0x69, 0x64,
				0x01, 0x00, 0x00, 0x00, 0x7B, 0x38, 0x31, 0x34, 0x31, 0x32, 0x44, 0x33,
				0x43, 0x2D, 0x32, 0x41, 0x32, 0x34, 0x2D, 0x34, 0x45, 0x39, 0x44, 0x2D,
				0x42, 0x32, 0x30, 0x45, 0x2D, 0x31, 0x31, 0x46, 0x37, 0x42, 0x42, 0x45,
				0x39, 0x32, 0x37, 0x39, 0x39, 0x7D, 0x00);
		String expected = Base64.encodeBase64String(expectedBytes);
		String actual = emailEncoder.msEventUidToGlobalObjId(
				new MSEventUid("{81412D3C-2A24-4E9D-B20E-11F7BBE92799}"));
		Assertions.assertThat(actual).isEqualTo(expected);
	}

}

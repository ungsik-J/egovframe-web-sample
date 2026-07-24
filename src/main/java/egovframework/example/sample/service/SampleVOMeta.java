package egovframework.example.sample.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.protobuf.FieldType;

public class SampleVOMeta {

	public static final LinkedHashMap<String, EgovField> EGOV_FIELD = new LinkedHashMap<>();

	static {
		EGOV_FIELD.put("ID", EgovField.builder().name("아이디").type(FieldType.STRING).byteSize(10).build());
	}

	public static int getEaiFieldToLength() {
		int egovFieldLen = 0;
		for (Map.Entry<String, EgovField> entry : EGOV_FIELD.entrySet()) {
			EgovField eaifield = entry.getValue();
			int length = eaifield.getByteSize();
			egovFieldLen += length;
		}
		return egovFieldLen;
	}
}

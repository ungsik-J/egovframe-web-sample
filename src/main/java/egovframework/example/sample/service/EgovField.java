package egovframework.example.sample.service;


import com.google.protobuf.FieldType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class EgovField extends EgovBaseDto{

	private String name;
	
	private FieldType type;
	
	private int byteSize;

}

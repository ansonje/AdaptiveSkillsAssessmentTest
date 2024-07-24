package Anson.AdaptiveSkillsAssessmentTest;

import java.util.List;

public class GPTResponse {
	private String id;
	private String object;
	private String created;
	private String model;
	
	private List<Choices> choices;
	
	private Usage usage;
	
	private String system_fingerprint;
	
	public String GetId() {
		return id;
	}
	
	public String GetObject() {
		return object;
	}
	
	public String GetCreated() {
		return created;
	}
	
	public String GetModel() {
		return model;
	}
	
	public String GetSystemFingerprint() {
		return system_fingerprint;
	}
	
	public Usage GetUsage() {
		return usage;
	}
	
	public List<Choices> GetChoices() {
		return choices;
	}
	
	public String GetContent() {
		return choices.get(0).GetMessages().GetContent();
	}
	
	public String GetRole() {
		return choices.get(0).GetMessages().GetRole();
	}
}

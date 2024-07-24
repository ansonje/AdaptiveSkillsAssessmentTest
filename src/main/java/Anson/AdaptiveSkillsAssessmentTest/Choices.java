package Anson.AdaptiveSkillsAssessmentTest;

public class Choices {
	private int index;
	
	private Message message;
	
	private String logprobs;
	private String finish_reason;
	
	public int GetIndex() {
		return index;
	}
	
	public String GetFinishReason() {
		return finish_reason;
	}
	
	public String GetLogprobs() {
		return logprobs;
	}
	
	public Message GetMessages(){
		return message;
	}
}

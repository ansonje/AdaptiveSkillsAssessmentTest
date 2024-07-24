package Anson.AdaptiveSkillsAssessmentTest;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.eclipse.wb.swing.FocusTraversalOnArray;
import java.awt.Component;
import java.awt.Window.Type;

public class App {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App window = new App();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// CHAT-GPT
	private String apiKey = "";
	
	// COMPONENTS
	private JLabel lblQuestionInfo;
	JPanel panel;
	// in panel
	private JLabel lblQuestion;
	private JLabel lblQuestionTitle;
	
	private JSlider slider;
	private JButton btnNext;
	
	// SETTINGS
	private final int totalSections = 3; // include section one in this whether or not you use it
	private void InitializeSettings(){
		// num of questions per section
		sections[0] = 5; // this section's questions are hand-made, set to 0 if you want to skip this
		sections[1] = 5;
		sections[2] = 5;
		InitializeSections();
		
		// section one questions
		sectOne[0] = "Are you confident in your ability to lead a team when necessary?";
		sectOne[1] = "Do you enjoy meeting new people and engaging in social activities?";
		sectOne[2] = "Do you often come up with new ideas or innovative solutions to problems?";
		sectOne[3] = "Do you prefer working in a collaborative team environment?";
		sectOne[4] = "Are you organized and able to keep track of multiple tasks effectively?";
	}
	
	private void InitializeSections() {
		for (int num : sections)
			totalQuestions += num;
		
		cTotalQuestions = sections[0];
		if (sections[0] <= 0) {
			sectionNum = 1;
			cTotalQuestions = sections[sectionNum];
			skip = true;
			return;
		}
		sectOne = new String[sections[0]];
		
		apiKey = JOptionPane.showInputDialog("INSERT API-KEY HERE");
	}
	
	// VARIABLES
	private String[] sectOne;
	private boolean skip = false;
	
	private int sectionNum = 0;
	private int[] sections = new int[totalSections];
	
	private int cTotalQuestions = 5;
	private int questionNum = 1;
	private int totalQuestions = 0;
	
	private String rawQuestion;
	
	// 8 = pre-determined number of skills being assessed (Skills: Extroversion, Leadership, Creativity, Teamwork, Organization, Communication, Flexibility, and Attention to detail.)
	private final int numOfSkills = 8;
	private Integer[] skillsAssessment = new Integer[numOfSkills];
	
	private List<String> questionHistory = new ArrayList<String>();
	private List<Integer> answerHistory = new ArrayList<Integer>();
	private List<Integer[]> skillsAssessmentHistory = new ArrayList<Integer[]>();
	
	/**
	 * Create the application.
	 */
	public App() {
		InitializeSettings();
		initialize();
		UpdateUI();
	}
	
	private void NextQuestion() {
		// setting the current question/section number
		
		if (questionNum < cTotalQuestions)
			questionNum++;
		else if (questionNum == cTotalQuestions) {
			if (sectionNum == 0)
				JOptionPane.showMessageDialog(null, "Section 1 is over. Every question on section 1 was hand-made. From this point onwards, the questions will be created by an AI catered to your responses", "New Section", JOptionPane.INFORMATION_MESSAGE); 
			else if (sectionNum >= sections.length - 1) {
				GPTCreateNextQuestion();
				EndQuiz();
				return;
			}
			sectionNum++;
			cTotalQuestions = sections[sectionNum];
			questionNum = 1;
		}
		rawQuestion = lblQuestion.getText().substring(9, lblQuestion.getText().length() - 11);
		
		// creating the next question 
		String nextQuestion;
		if (sectionNum != 0) {
			nextQuestion = GPTCreateNextQuestion(); 
		} else {
			GPTAnalyzeAnswer();
			nextQuestion = sectOne[questionNum-1];
		} 			
		
		// saving current stats
		questionHistory.add(lblQuestion.getText().substring(9, lblQuestion.getText().length() - 11));
		answerHistory.add(slider.getValue());
		skillsAssessmentHistory.add(skillsAssessment.clone());
		
		// setting the next question
		lblQuestion.setText("<html><p>%s</p></html>".formatted(nextQuestion));
		
		//printing stats
		System.out.println("__________________________________________________________________________________END OF NEXT QUESTION__________________________________________________________________________________");
		slider.setValue(50);
		UpdateUI();
		
		// TEMP
		for (int i = 0; i < questionHistory.size(); i++) {
			System.out.println("Assessment score %s: Extroversion: %s, Leadership: %s, Creativity: %s, Teamwork: %s, Organization: %s, Communication: %s, Flexibility: %s, Attention to detail: %s     ".formatted(i, 
					skillsAssessmentHistory.get(i)[0], skillsAssessmentHistory.get(i)[1], skillsAssessmentHistory.get(i)[2], skillsAssessmentHistory.get(i)[3], 
					skillsAssessmentHistory.get(i)[4], skillsAssessmentHistory.get(i)[5], skillsAssessmentHistory.get(i)[6], skillsAssessmentHistory.get(i)[7]));
		}
		System.out.println();
	}
	
	private void UpdateUI() {
		lblQuestionInfo.setText("Question %s/%s     Section %s/%s".formatted(questionNum, cTotalQuestions, skip?sectionNum:sectionNum+1, skip?totalSections-1:totalSections));
		lblQuestionTitle.setText("Question %s".formatted(questionNum));
	}
	
	private void EndQuiz() {
		List<String> roles = new ArrayList<String>(), messages = new ArrayList<String>();
		roles.add("user");
		messages.add("Give me 5 job titles based on my Skills Assessment Score");
		roles.add("system");
		messages.add("a 'Skills Assessment Score' is a number between 0(negative) and 100(positive) for the following skills: "
				+ "Extroversion, Leadership, Creativity, Teamwork, Organization, Communication, Flexibility, Attention to detail. "
				+ "your only goal is to give the user 5 different job titles that best suit their top 3 skills, only give the user the titles, nothing else. "
				+ "User's score: Extroversion: %s, Leadership: %s, Creativity: %s, Teamwork: %s, Organization: %s, Communication: %s, Flexibility: %s, Attention to detail: %s".formatted(
						skillsAssessment[0], skillsAssessment[1], skillsAssessment[2], skillsAssessment[3], skillsAssessment[4], skillsAssessment[5], skillsAssessment[6], skillsAssessment[7]));
		
		btnNext.setEnabled(false);
		JOptionPane.showMessageDialog(null, "Good job! You have successfully finished the Adaptive Skills Assessment Test.\n"
				+ "Your Score: Extroversion:\n%s\nLeadership: %s\nCreativity: %s\nTeamwork: %s\nOrganization: %s\nCommunication: %s\nFlexibility: %s\nAttention to detail: %s\n\n".formatted(
						skillsAssessment[0], skillsAssessment[1], skillsAssessment[2], skillsAssessment[3], skillsAssessment[4], skillsAssessment[5], skillsAssessment[6], skillsAssessment[7])
				+ "\nHere are some job titles that suit your skills best:\n" + AskGPT(roles, messages).GetContent(), 
				
				"You finished!", JOptionPane.PLAIN_MESSAGE); 
	}
	
	// CHAT-GPT
	private String GPTCreateNextQuestion() {
		List<String> roles = new ArrayList<String>(), messages = new ArrayList<String>();
		
		// given information
		roles.add("system");
		messages.add("Your only goal is to find out what the user's 'Skills Assessment Score' is based on their answers to your questions, "
				+ "you will be given their 'Skills Assessment Score' so far, and you will give them a question based on that.     "
				+ "if a score stays at either 100 or 0 for 3 questions, ask a question about that topic. Try to keep their 'Skills Assessment Scores' away from 0 and away from 100."
				+ "TAKE QUESTION/ANSWER HISTORY INTO CONSIDERATION. BE HARSH WITH THE SCORES GIVEN. "
				+ "only give the user a MAXIMUM of 4 topics with a score higher than 80 and a MINIMUM of 1 with a score higher than 80, and give a MAXIMUM of 1 score below 20.     "
				+ "you want the user to have at least 5 scores above 50. Overall try to keep their 'Skills Assessment Scores' at 10+. "
				+ "MAKE SURE TO CHANGE ATLEAST ONE OF THE SCORES BY 5-10 ALMOST EVERY TIME A QUESTION IS ANSWERED"
				+ "the user will be asked %s questions.     ".formatted(totalQuestions)
				+ "a 'Skills Assessment Score' is a number between 0(negative) and 100(positive) for the following topics: "
				+ "Extroversion, Leadership, Creativity, Teamwork, Organization, Communication, Flexibility, Attention to detail.     "
				+ "Users current 'Skills Assessment Score': "
				+ "Extroversion: 0, Leadership: 0, Creativity: 0, Teamwork: 0, Organization: 0, Communication: 0, Flexibility: 0, Attention to detail: 0     "
				+ "ONLY ASK YES OR NO QUESTIONS. ONLY RESPOND WITH THE QUESTION AND THEIR UPDATED SKILLS ASSESSMENT SCORE BASED ON THE LAST QUESTION ASKED IN THIS FORMAT: "
				+ "<QUESTION>|<EXTROVERSION SCORE>,<LEADERSHIP SCORE>,<CREATIVITY SCORE>,<TEAMWORK SCORE>,<ORGANIZATION SCORE>,<COMMUNICATION>,<FLEXIBILITY SCORE>,<ATTENTION TO DETAIL SCORE>. DO NOT ASK REPEAT QUESTIONS. "
				+ "THE USER WILL RESPOND WITH A NUMBER IN-BETWEEN 0-100, AGREE-DISAGREE.");
		
		// question/answer/skills assessment score history
		System.out.println("History:");
		for (int i = 0; i < questionHistory.size(); i++) {
			roles.add("assistant");
			messages.add("%s|%s,%s,%s,%s,%s,%s,%s,%s".formatted(questionHistory.get(i), 
					skillsAssessmentHistory.get(i)[0], skillsAssessmentHistory.get(i)[1], skillsAssessmentHistory.get(i)[2], skillsAssessmentHistory.get(i)[3], 
					skillsAssessmentHistory.get(i)[4], skillsAssessmentHistory.get(i)[5], skillsAssessmentHistory.get(i)[6], skillsAssessmentHistory.get(i)[7]));
			System.out.println("Question %s: %s ".formatted(i, questionHistory.get(i)));
			
			roles.add("user");
			messages.add(answerHistory.get(i).toString());
			System.out.println("Answer %s: %s ".formatted(i, answerHistory.get(i)));

			System.out.println("Assessment score %s: Extroversion: %s, Leadership: %s, Creativity: %s, Teamwork: %s, Organization: %s, Communication: %s, Flexibility: %s, Attention to detail: %s     ".formatted(i, 
					skillsAssessmentHistory.get(i)[0], skillsAssessmentHistory.get(i)[1], skillsAssessmentHistory.get(i)[2], skillsAssessmentHistory.get(i)[3], 
					skillsAssessmentHistory.get(i)[4], skillsAssessmentHistory.get(i)[5], skillsAssessmentHistory.get(i)[6], skillsAssessmentHistory.get(i)[7]));
			System.out.println();
		}
		
		roles.add("assistant");
		messages.add(rawQuestion);
		System.out.println("Last question: " + rawQuestion);
		
		roles.add("user");
		messages.add(Integer.toString(slider.getValue()));
		System.out.println("Last response: " + slider.getValue());
		
		roles.add("system");
		messages.add("Extroversion: %s, Leadership: %s, Creativity: %s, Teamwork: %s, Organization: %s, Communication: %s, Flexibility: %s, Attention to detail: %s     ".formatted(
				skillsAssessment[0], skillsAssessment[1], skillsAssessment[2], skillsAssessment[3], skillsAssessment[4], skillsAssessment[5], skillsAssessment[6], skillsAssessment[7]));
		System.out.println("Last assessment score: Extroversion: %s, Leadership: %s, Creativity: %s, Teamwork: %s, Organization: %s, Communication: %s, Flexibility: %s, Attention to detail: %s     ".formatted(
				skillsAssessment[0], skillsAssessment[1], skillsAssessment[2], skillsAssessment[3], skillsAssessment[4], skillsAssessment[5], skillsAssessment[6], skillsAssessment[7]));
		
		GPTResponse response = AskGPT(roles, messages);
		String content = response.GetContent();
		
		System.out.println("CONTENT OF GPT: " + content);
		// seperate question and skill assessment, and update both
		int index = 0;
		String score = "";
		for (int i = content.indexOf("|")+1; i < content.length(); i++) {
			if (content.charAt(i) == ',') {
				skillsAssessment[index] = Integer.valueOf(score);
				System.out.print("SCORE %s: %s, ".formatted(index, score));
				score = "";
				index++;
				continue;
			}
			score += content.charAt(i);
		}
		skillsAssessment[index] = Integer.valueOf(score);
		System.out.println("SCORE %s: %s\n".formatted(index, score));
		
		return content.substring(0, content.indexOf("|"));
	}
	
	private void GPTAnalyzeAnswer() {
		List<String> roles = new ArrayList<String>(), messages = new ArrayList<String>();
		
		// given information
		roles.add("system");
		messages.add("Your only goal is to find out what the user's 'Skills Assessment Score' is based on their answer to the last question, "
				+ "you will be given their 'Skills Assessment Score' so far, and you will add from it or remove from it based on their answer, then return it. "
				+ "TAKE QUESTION/ANSWER HISTORY INTO CONSIDERATION. BE HARSH WITH THE SCORES GIVEN. "
				+ "only give the user a max of 4 topics with a score higher than 80, and give a max of 1 score below 20.     "
				+ "you want the user to have around 5 scores above 50. try to keep their 'Skills Assessment Scores' away from 0 and away from 100. "
				+ "the user will be asked %s questions. make sure that the rules stated above are true by the last question     ".formatted(totalQuestions)
				+ "a 'Skills Assessment Score' is a number between 0(negative) and 100(positive) for the following topics: "
				+ "Extroversion, Leadership, Creativity, Teamwork, Organization, Communication, Flexibility, Attention to detail.     "
				+ "Questions are answered with a number in-between 0-100, Agree-Disagree     "
				+ "Users current 'Skills Assessment Score': "
				+ "Extroversion: 0, Leadership: 0, Creativity: 0, Teamwork: 0, Organization: 0, Communication: 0, Flexibility: 0, Attention to detail: 0     "
				+ "ONLY RESPOND IN THIS FORMAT. NO SPACES, A COMMA BETWEEN EACH SCORE, ONLY NUMBERS.     "
				+ "FORMAT: <EXTROVERSION SCORE>,<LEADERSHIP SCORE>,<CREATIVITY SCORE>,<TEAMWORK SCORE>,<ORGANIZATION SCORE>,<COMMUNICATION>,<FLEXIBILITY SCORE>,<ATTENTION TO DETAIL SCORE>     "
				+ "THE USER WILL RESPOND WITH THE QUESTION THAT WAS ASKED THEM AND THE ANSWER THEY GAVE IN THE FORMAT: <QUESTION>|<ANSWER>.");
		
		// question/answer/skills assessment score history
		for (int i = 0; i < questionHistory.size(); i++) {
			roles.add("assistant");
			messages.add("%s,%s,%s,%s,%s,%s,%s,%s".formatted(
					skillsAssessmentHistory.get(i)[0], skillsAssessmentHistory.get(i)[1], skillsAssessmentHistory.get(i)[2], skillsAssessmentHistory.get(i)[3], 
					skillsAssessmentHistory.get(i)[4], skillsAssessmentHistory.get(i)[5], skillsAssessmentHistory.get(i)[6], skillsAssessmentHistory.get(i)[7]));
			
			roles.add("user");
			messages.add("%s|%s".formatted(questionHistory.get(i), answerHistory.get(i).toString()));
		}
		roles.add("user");
		messages.add("%s|%s".formatted(rawQuestion, Integer.toString(slider.getValue())));
		
		roles.add("assistant");
		messages.add("%s,%s,%s,%s,%s,%s,%s,%s".formatted(
				skillsAssessment[0], skillsAssessment[1], skillsAssessment[2], skillsAssessment[3], skillsAssessment[4], skillsAssessment[5], skillsAssessment[6], skillsAssessment[7]));
		
		GPTResponse response = AskGPT(roles, messages);
		
		// checks to see if the user gave us a working api key.
		try {
			System.out.println("Analyze answer content: " + response.GetContent());
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(null, "Hmmm, it seems like you didn't input a working API-key, let's try that again", "Hmmm...🤔", JOptionPane.PLAIN_MESSAGE);
				apiKey = JOptionPane.showInputDialog("INSERT API-KEY HERE");
				questionNum = 1;
				sectionNum = 0;
				return;
			}
		
		// update skills assessment
		int index = 0;
		String score = "";
		for (int i = 0; i < response.GetContent().length(); i++) {
			if (response.GetContent().charAt(i) == ',') {
				skillsAssessment[index] = Integer.valueOf(score);
				System.out.print("SCORE %s: %s, ".formatted(index, score));
				score = "";
				index++;
				continue;
			}
			score += response.GetContent().charAt(i);
		}
		skillsAssessment[index] = Integer.valueOf(score);
		System.out.println("SCORE %s: %s\n".formatted(index, score));
	}
	
	private GPTResponse AskGPT(List<String> roles, List<String> messages) {
		// make sure equal roles and messages were given
		if (roles.size() != messages.size())
			return null;
		// making request body
		String body = """
		{
			"model": "gpt-4o-mini",
			"messages": [
				{
					"role": "%s",
					"content": "%s"
				}
			""".formatted(roles.get(0), messages.get(0));
		
		for (int i = 1; i < roles.size(); i++) {
			body += """
, 
				{
					"role": "%s",
					"content": "%s"
				}""".formatted(roles.get(i), messages.get(i));
		}
		
		body += """
			]
		}""";
		
		// create request
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.openai.com/v1/chat/completions"))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + apiKey)
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		
		GPTResponse response = null;
		try {
			// send request
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> _response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
			// deserialize response
			Gson gson = new GsonBuilder().create();
			response = gson.fromJson(_response.body(), GPTResponse.class);
			//System.out.println(_response.body());
		} catch (Exception e) { System.out.println(e.getMessage()); }
		
		return response;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setType(Type.UTILITY);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(App.class.getResource("/resources/skills-svgrepo-com.png")));
		frame.setTitle("Adaptive Skills Assessment Test");
		frame.setBounds(100, 100, 1600, 900);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		lblQuestionInfo = new JLabel("Question 1/5     Section 1/2");
		lblQuestionInfo.setFont(new Font("NewsGoth BT", Font.BOLD, 11));
		lblQuestionInfo.setHorizontalAlignment(SwingConstants.CENTER);
		
		panel = new JPanel();
		panel.setBackground(new Color(255, 128, 128));
		// IN PANEL
		lblQuestionTitle = new JLabel("Question 1: ");
		lblQuestionTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblQuestionTitle.setFont(new Font("Raleway Medium", Font.PLAIN, 38));
		lblQuestionTitle.setToolTipText("");
		
		lblQuestion = new JLabel("<html><p>%s</p></html>".formatted(sectOne[0]));
		lblQuestion.setFont(new Font("Square721 Cn BT", Font.PLAIN, 30));
		lblQuestion.setVerticalAlignment(SwingConstants.TOP);
		lblQuestion.setHorizontalAlignment(SwingConstants.CENTER);
		
		slider = new JSlider();
		slider.setForeground(new Color(255, 128, 128));
		slider.setBackground(new Color(255, 128, 128));
		
		btnNext = new JButton("Next");
		btnNext.setFont(new Font("Raleway", Font.PLAIN, 15));
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NextQuestion();
			}
		});
		
		JLabel lblNo = new JLabel("Disagree");
		lblNo.setFont(new Font("Square721 BT", Font.PLAIN, 12));
		lblNo.setForeground(new Color(0, 0, 0));
		lblNo.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JLabel lblYes = new JLabel("Agree");
		lblYes.setForeground(new Color(0, 0, 0));
		lblYes.setFont(new Font("Square721 BT", Font.PLAIN, 12));
		lblYes.setVerticalAlignment(SwingConstants.BOTTOM);
		lblYes.setHorizontalAlignment(SwingConstants.LEFT);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(10)
					.addComponent(lblQuestionTitle, GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
					.addGap(10))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(10)
					.addComponent(lblQuestion, GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
					.addGap(10))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(lblNo, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(slider, GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
					.addGap(6)
					.addComponent(lblYes, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
					.addGap(3))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(29)
					.addComponent(btnNext, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
					.addGap(29))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(lblQuestionTitle)
					.addGap(1)
					.addComponent(lblQuestion, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
					.addGap(13)
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
								.addComponent(slider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNo, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED))
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(lblYes, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
							.addGap(9)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNext, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addComponent(lblQuestionInfo, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(20)
							.addComponent(panel, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)))
					.addGap(10))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(11)
					.addComponent(lblQuestionInfo)
					.addGap(11)
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
					.addGap(11))
		);
		frame.getContentPane().setLayout(groupLayout);
		frame.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{slider, btnNext}));
	}
}

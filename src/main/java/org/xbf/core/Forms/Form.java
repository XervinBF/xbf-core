package org.xbf.core.Forms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.LoggerFactory;
import org.xbf.core.Messages.Pair;
import org.xbf.core.Messages.RichResponse;
import org.xbf.core.Models.XUser;

import ch.qos.logback.classic.Logger;

public class Form {

	public String channel;
	public XUser user;
	
	public int step;
	public ArrayList<Pair<String, List<String>>> questions = new ArrayList<>();
	public ArrayList<String> answers = new ArrayList<>();
	Logger l;
	public String name;
	
	public Form(String name) {
		if(name == null) {
			l = (Logger) LoggerFactory.getLogger("Form - unnamed");
		} else {
			l = (Logger) LoggerFactory.getLogger("Form - " + name);
			this.name = name;
		}
	}
	
	public Form addQuestion(String string) {
		return addQuestion(string, new ArrayList<String>());
	}
	
	public Form addQuestion(String question, List<String> alternatives) {
		questions.add(new Pair<String, List<String>>(question, alternatives));
		return this;
	}
	
	public RichResponse handleAnswer(String answer) {
		l.info("Q" + step + ": " + answer);
		answers.add(answer);
		step++;
		Pair<String, List<String>> p = answerHandler.apply(new Pair<Form, String>(this, answer));
		if(p != null)
			return build(p);
		FormManager.endForm(this);
		return formComplete.apply(answers);
	}
	
	public 
	
	Function<Pair<Form, String>, Pair<String, List<String>>> answerHandler = (pair) -> {
		if(questions.size() == step) return null;
		return questions.get(step);
	};
	
	public Form setHandler(Function<Pair<Form, String>, Pair<String, List<String>>> handler) {
		answerHandler = handler;
		return this;
	}
	
	Function<ArrayList<String>, RichResponse> formComplete;
	
	public Form onComplete(Function<ArrayList<String>, RichResponse> handler) {
		formComplete = handler;
		return this;
	}
	
	public RichResponse build() {
		return build(answerHandler.apply(new Pair<Form, String>(this, null)));
	}
	
	public RichResponse build(Pair<String, List<String>> p) {
		RichResponse r = new RichResponse(p.getKey());
		for (String v : p.getValue()) {
			r.addCommand(v, v);
		}
		return r;
	}


	
	
	
	
}

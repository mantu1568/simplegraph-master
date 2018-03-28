
package examples;

import personalised.library.GraphDB.*;
import java.util.*;

public class Example2 {
    private static long begin=0, end=0;

    // TESTS WRITTEN IN THE MAIN METHOD BELOW
	
	public static void main(String[] args) {
		try {
			SimpleGraph g = get_sample_graph(false);
			
			boolean queryOnly = false;
			
			if (queryOnly){
				List<String[]> query = new ArrayList<String[]>();
				/*
				query.add(new String[]{"?dad", "employer", "consulting_corp"});
				query.add(new String[]{"?dad", "title", "nerd"});
				query.add(new String[]{"?dad", "gender", "male"});
				query.add(new String[]{"?dad", "child", "girl"});
				*/
				
				// query.add(new String[]{"?dude", "married_to", "woman"});
				query.add(new String[]{"?dude", "name", "?name"});
				
				startNanoTimer();
				Map<String, Set<String>> result = (Map<String, Set<String>>) g.query(query);
				stopNanoTimer();
				
				System.out.println(result);
				nanoTimerPrint();
	
			}
			else{
				
				startNanoTimer();
				Map<String, Map<String, Set<String>>> triples = (Map<String, Map<String, Set<String>>>) g.triples(null, null, null);
				String subject, predicate;
				long counter = 0;
				for (Iterator<String> itr1=triples.keySet().iterator(); itr1.hasNext(); ){
					subject = itr1.next();
					for (Iterator<String> itr2=g.map(subject, null, null).keySet().iterator(); itr2.hasNext(); ){
						predicate = itr2.next();
						counter+=g.list(subject, predicate, null).size();
					}
				}
				stopNanoTimer();
				System.out.println("It took " + nanoTimerAsString() + " to count " + counter + " triples!");
				
				// Check if dude lives in Sometown
				System.out.println(g.is("dude", "hometown", "sometown"));
				
				// Check if dude is married to Woman
				System.out.println(g.is("woman", "married_to", "dude"));
				
				// Check the ID's of dudes children (if any)
				System.out.println(g.list("dude", "child", null));
				
				// Show all properties of dude
				System.out.println(g.map("dude", null, null));
				
				// Show people with home town sometown
				System.out.println(g.list(null, "hometown", "sometown"));
				
				// Check if womans home town is othertown
				System.out.println(g.is("woman", "hometown", "othertown"));
				
				// Get names of Womans children that are male (=boys)
				for (String id: g.list("woman", "child", null)){
					if (g.is(id, "gender", "male")){
						System.out.println(g.value(id, "name", null));
					}
				}
				
				/*
				 * The SQL for the above is sort of
				 * 
				 * select p.name from person as p
				 * 	where p.gender='male'
				 * inner join children as c
				 * 	on c.child_id=p.id
				 *	where c.parent_id='woman'
				 */
				
				// Get the ID of the person named "Girl Girlsen"
				startNanoTimer();
				g.value(null, "name", "Girl Girlsen");
				stopNanoTimer();
				nanoTimerPrint();
				
				System.out.println("---");
				Map<String, Set<String>> companiesAndNames = new HashMap<String, Set<String>>();
				
				startNanoTimer();
				
				/*
				 *  1: find boy_1 id
				 *  2: find boy_1 parent who is a man (=dad)
				 *  3: find names of the persons that work same place, as the one married to one in the dads network, working at Consulting Corp
				 */
				String boy = g.value(null, "name", "Boy Oneson");
				for (String parent: g.list(null, "child", boy)){
					if (g.is(parent, "gender", "male")){
						for (String someone_male_parent_knows: g.list(parent, "knows", null)){
							if (g.is(someone_male_parent_knows, "employer", "consulting_corp")){
								for (String partner: g.list(someone_male_parent_knows, "married_to", null)){
									String employerId = g.value(partner, "employer", null);
									String employerName = g.value(employerId, "name", null);
									Set<String> names = companiesAndNames.get(employerName);
									if (names==null){
										names = new HashSet<String>();
										companiesAndNames.put(employerName, names);
									}
									for(String colleague: g.list(null, "employer", employerId)){
										names.add(g.value(colleague, "name", null));
									}
								}
							}
						}
					}
				}
				stopNanoTimer();
				nanoTimerPrint();
	
				StringBuffer result = new StringBuffer();
				String company;
				for (Iterator<String> itr=companiesAndNames.keySet().iterator(); itr.hasNext(); ){
					company = itr.next();
					result.append(company).append(":\n");
					for (String name: companiesAndNames.get(company)){
						result.append("\t").append(name).append("\n");
					}
				}
				System.out.println("Spouses and their colleagues, to those who knows boy_1's dad working at Consulting Corp:\n" + result.toString());
				/*
				System.out.println("---");
				startNanoTimer();
				System.out.println(g.map(null, "likes", null));
				stopNanoTimer();
				System.out.println("it took " + nanoTimerAsString() + " to find everything that likes something!");
				*/
			}
		}
		catch (GraphException e) {
			e.printStackTrace();
		}
	}


// SAMPLE GRAPH DATASET BELOW
	
	private static SimpleGraph get_sample_graph(boolean huge) throws GraphException {
		SimpleGraph g = new SimpleGraph();
		
		g.add("dude", "married_to", "woman");
		g.add("dude", "kids_with", "woman");
		g.add("dude", "child", "boy_1");
		g.add("dude", "child", "boy_2");
		g.add("dude", "child", "girl");
		g.add("dude", "hometown", "sometown");
		g.add("dude", "name", "Dude Dudeson");
		g.add("dude", "gender", "male");
		g.add("dude", "age", "42");
		g.add("dude", "title", "nerd");
		g.add("dude", "employer", "consulting_corp");
		
		g.add("woman", "name", "Woman Womansen");
		g.add("woman", "married_to", "dude");
		g.add("woman", "kids_with", "dude");
		g.add("woman", "child", "boy_1");
		g.add("woman", "child", "boy_2");
		g.add("woman", "child", "girl");
		g.add("woman", "gender", "female");
		g.add("woman", "hometown", "sometown");
		g.add("woman", "age", "41");
		g.add("woman", "title", "doctor");
		
		g.add("boy_1", "name", "Boy Oneson");
		g.add("boy_1", "gender", "male");
		g.add("boy_1", "father", "dude");
		g.add("boy_1", "mother", "woman");
		g.add("boy_1", "brother", "boy_2");
		g.add("boy_1", "sister", "girl");
		g.add("boy_1", "age", "14");
		g.add("boy_1", "title", "skoleelev");
		
		g.add("boy_2", "name", "Boy Twoson");
		g.add("boy_2", "gender", "male");
		g.add("boy_2", "father", "dude");
		g.add("boy_2", "mother", "woman");
		g.add("boy_2", "brother", "boy_1");
		g.add("boy_2", "sister", "girl");
		g.add("boy_2", "age", "10");

		g.add("girl", "name", "Girl Girlsen");
		g.add("girl", "gender", "female");
		g.add("girl", "father", "dude");
		g.add("girl", "mother", "woman");
		g.add("girl", "brother", "boy_1");
		g.add("girl", "brother", "boy_2");
		g.add("girl", "age", "8");
		g.add("girl", "title", "pupil");
		
		g.add("inlaw_1", "name", "Inlaw Oneson");
		g.add("inlaw_1", "child", "woman");
		g.add("inlaw_1", "married_to", "inlaw_2");
		g.add("inlaw_1", "employer", "inlaw_workplace");
		g.add("inlaw_1", "child", "extra_dude_1");
		g.add("inlaw_1", "child", "extra_dude_2");
		g.add("inlaw_1", "title", "doctor");
		
		g.add("extra_dude_1", "name", "Extra Dude Oneson");
		g.add("extra_dude_1", "title", "doctor");
		g.add("extra_dude_2", "name", "Extra Dude Twoson");
		g.add("extra_dude_2", "title", "doctor");
		g.add("inlaw_2", "name", "Inlaw Twoson");
		g.add("inlaw_2", "employer", "inlaw_workplace");
		g.add("inlaw_2", "title", "teacher");
		g.add("inlaw_workplace", "name", "Inlaw Workplace");
		
		g.add("sometown", "name", "Sometown");
		g.add("othertown", "name", "Othertown");
		
		g.add("dude", "knows", "other_dude");
		g.add("other_dude", "name", "Other Dude");
		g.add("other_dude", "married_to", "other_woman");
		g.add("other_dude", "employer", "consulting_corp");
		g.add("other_dude", "title", "nerd");
		g.add("other_dude", "gender", "male");
		g.add("other_woman", "name", "Other Woman");
		g.add("other_woman", "employer", "public_workplace");
		g.add("other_woman", "title", "physical_stuff");
		g.add("public_workplace", "name", "Public Workplace");
		g.add("public_workplace", "manager", "public_manager");
		g.add("public_manager", "name", "Public Manager");
		g.add("public_manager", "employer", "public_workplace");
		g.add("dude", "knows", "colleague_1");
		g.add("colleague_1", "name", "Colleage Oneson");
		g.add("colleague_2", "name", "Colleague Twoson");
		g.add("dude", "knows", "colleague_2");
		g.add("colleague_2", "employer", "consulting_corp");
		g.add("colleague_2", "title", "dr_evil");
		g.add("colleague_1", "employer", "consulting_corp");
		g.add("some_wife", "name", "Some Wife");
		g.add("some_wife", "title", "nerd");
		g.add("colleague_2", "married_to", "some_wife");
		g.add("colleagues_wife", "name", "Colleagues Wife");
		g.add("colleagues_wife", "employer", "maternity_leave");
		g.add("colleague_1", "married_to", "colleagues_wife");
		g.add("some_wife", "employer", "consulting_corp");
		g.add("maternity_leave", "name", "Maternity Leave");
		g.add("consulting_corp", "name", "Consulting Corp");
		g.add("business_guy", "name", "Business Guy");
		g.add("business_guy", "married_to", "lawyer_woman");
		g.add("business_guy", "employer", "consulting_corp");
		g.add("lawyer_woman", "name", "Lawyer Woman");
		g.add("lawyer_woman", "employer", "lawyer_corp");
		g.add("lawyer_corp", "name", "Lawyer Corp");
		g.add("lawyer_2", "name", "Lawyer 2");
		g.add("lawyer_2", "employer", "lawyer_corp");
		g.add("lawyer_3", "name", "Lawyer 3");
		g.add("lawyer_3", "employer", "lawyer_corp");
		g.add("lawyer_4", "name", "Lawyer 4");
		g.add("lawyer_4", "employer", "lawyer_corp");
		g.add("dude", "knows", "business_guy");
		g.add("dude", "likes", "http://news.ycombinator.com/");
		g.add("post_1", "person", "dude");
		g.add("post_1", "tekst", "This is the first post from Dude One on the social graph!");
		g.add("post_1", "tid", "2011-11-17@2059");
		g.add("post_1", "link", "http://dude.dude");
		g.add("other_dude", "likes", "post_1");
		
		g.add("old_dude", "title", "nerd");
		
		if (huge){
			// add more data
			String object1;
			SimpleGraph gMultiplied = new SimpleGraph();
			startNanoTimer();
			Map<String, Set<String>> map1 = (Map<String, Set<String>>) g.triples(null, "name", null);
			for (Iterator<String> itr1=map1.keySet().iterator(); itr1.hasNext();){
				object1 = itr1.next(); // all triples with predicate "name"
				for (String subject1: map1.get(object1)){
					Map<String, Set<String>> predicates2 = g.map(subject1, null, null);
					String predicate2;
					for (Iterator<String> itr2 = predicates2.keySet().iterator(); itr2.hasNext(); ){
						// iterate all triples for this subject
						predicate2 = itr2.next();
						for (String object2: predicates2.get(predicate2)){
							gMultiplied.add(subject1, predicate2, object2); // add original triple
							Map<String,Set<String>> map3 = g.map(object2, null, null);
							if (map3!=null && map3.size()>0){
								// object is in itself a subject!
								for (int i=0; i<10000; i++){
									gMultiplied.add(subject1+"_"+i, predicate2, object2+"_"+i);
								} 
							}
							else {
								// only the subject is a subject (the object is a value)
									for (int i=0; i<10000; i++){
										if (predicate2.equals("name")){
											//change also the name
											gMultiplied.add(subject1+"_"+i, predicate2, object2 + "_" + i);
										}
										else {
											// change only the subject
											gMultiplied.add(subject1+"_"+i, predicate2, object2);
										}
									}
								
							}
						}
						
					}
				}
			}
			stopNanoTimer();
			System.out.println("it took " + (nanoTimerAsString()) + " to multiply the data size");
					
			// Remove reference to original graph, and use the new multiplied instead!
			g = gMultiplied;
		}
		return g;
	}
	
// Just some simple timer helpers below..

	private static void startNanoTimer(){
		begin = System.nanoTime();
	}
	
	private static void stopNanoTimer(){
		end = System.nanoTime();
	}
	
	private static String nanoTimerAsString(){
		return "" + (end-begin)/1000 + " micros";
	}
	
	private static void nanoTimerPrint(){
		System.out.println(nanoTimerAsString());
	}
}
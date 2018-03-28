package personalised.library.GraphDB;

// --- IMPLEMENTATION START ---

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class SimpleGraph {
	
	private Map<String, Map<String, Set<String>>>
		_spo = null,
		_pos = null,
		_osp = null;
	private String fileName;
		
	public SimpleGraph() {
		_spo = new HashMap<String, Map<String,Set<String>>>();
		_pos = new HashMap<String, Map<String, Set<String>>>();
		_osp = new HashMap<String, Map<String, Set<String>>>();
		this.fileName = "";
	}

	public SimpleGraph(String fileName){
		_spo = new HashMap<String, Map<String,Set<String>>>();
		_pos = new HashMap<String, Map<String, Set<String>>>();
		_osp = new HashMap<String, Map<String, Set<String>>>();
		this.fileName = fileName;
	}
	
	public void add(String subject, String predicate, String object){
		if (subject!=null && predicate!=null && object!=null){
			addToIndex(_spo, subject, predicate, object);
			addToIndex(_pos, predicate, object, subject);
			addToIndex(_osp, object, subject, predicate);
		}
	}

	public void delete(String subject, String predicate, String object) throws GraphException {
		if (subject != null && predicate != null && object != null){
			removeFromIndex(_spo, subject, predicate, object);
			removeFromIndex(_pos, predicate, object, subject);
			removeFromIndex(_osp, object, subject, predicate);
		}
	}

	public void update(String subject, String predicate, String object, String updateChoice, String newValue) throws GraphException {
		if (subject != null && predicate != null && object != null) {
			if (updateChoice.toLowerCase().equals("s") || updateChoice.toLowerCase().equals("o") || updateChoice.toLowerCase().equals("p")){
				if (updateChoice.toLowerCase().equals("s")){
					updateIndex(_spo, subject, predicate, object, "a", newValue);
					updateIndex(_pos, predicate, object, subject, "c", newValue);
					updateIndex(_osp, object, subject, predicate, "b", newValue);
				} else if (updateChoice.toLowerCase().equals("o")) {
					updateIndex(_spo, subject, predicate, object, "c", newValue);
					updateIndex(_pos, predicate, object, subject, "b", newValue);
					updateIndex(_osp, object, subject, predicate, "a", newValue);
				} else if (updateChoice.toLowerCase().equals("p")) {
					updateIndex(_spo, subject, predicate, object, "b", newValue);
					updateIndex(_pos, predicate, object, subject, "a", newValue);
					updateIndex(_osp, object, subject, predicate, "c", newValue);
				}
			} else {
				throw new GraphException("Illegal update choice");
			}
		}
	}

	public void addFile(String fileName) throws GraphException {
		if (!this.fileName.equals(""))
			throw new GraphException("File Name already specified");
		this.fileName = fileName;
	}

	public SimpleGraph load() throws GraphException, IOException, FileNotFoundException {
		if (fileName.equals(""))
			throw new GraphException("No File Name specified");
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		LinkedList<Map<String, Map<String, Set<String>>>> listOfObjects;
		try{
			listOfObjects = (LinkedList<Map<String, Map<String, Set<String>>>>) ois.readObject();
			_spo = listOfObjects.get(0);
			_pos = listOfObjects.get(1);
			_osp = listOfObjects.get(2);
		} catch(ClassNotFoundException e){
			throw new GraphException("Corrupted Database File");
		}
		return this;
	}

	public void commit() throws GraphException, IOException {
		if (fileName.equals(""))
			throw new GraphException("No File Name specified");
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		LinkedList<Map<String, Map<String, Set<String>>>> listOfObjects = new LinkedList<>();
		listOfObjects.add(_spo);
		listOfObjects.add(_pos);
		listOfObjects.add(_osp);
		oos.writeObject(listOfObjects);
		oos.close();
	}

	private void updateIndex(Map<String, Map<String, Set<String>>> index, String a, String b, String c, String updateChoice, String newValue) throws GraphException {
		Map<String, Set<String>> _map = index.get(a);
		if (_map == null){
			throw new GraphException("First Argument node not found.");
		} else if (updateChoice.equals("a")) {
			Map<String, Set<String>> tempMap = index.get(a);
			index.remove(a);
			index.put(newValue, tempMap);
		}

		Set<String> _set = _map.get(b);
		if (_set == null){
			throw new GraphException("Second Argument node not found.");
		} else if (updateChoice.equals("b")) {
			Set<String> tempSet = _map.get(b);
			_map.remove(b);
			_map.put(newValue, tempSet);
		}

		if (_set.contains(c)){
			if (updateChoice.equals("c")) {
				_set.remove(c);
				_set.add(newValue);
			}
		} else {
			throw new GraphException("Third Argument node not found.");
		}
	}

	private void removeFromIndex(Map<String, Map<String, Set<String>>> index, String a, String b, String c) throws GraphException{
		Map<String, Set<String>> _map = index.get(a);
		if (_map == null){
			throw new GraphException("First Argument node not found.");
		}

		Set<String> _set = _map.get(b);
		if (_set == null){
			throw new GraphException("Second Argument node not found.");
		}

		if (_set.contains(c)){
			_set.remove(c);
		} else {
			throw new GraphException("Third Argument node not found.");
		}
	}
	
	private void addToIndex(Map<String, Map<String, Set<String>>> index, String a, String b, String c){
		Map<String,Set<String>> _map = index.get(a);
		if (_map==null){
			_map = new HashMap<String, Set<String>>();
			index.put(a, _map);
		}
		
		Set<String> _set = _map.get(b);
		if (_set==null){
			_set = new HashSet<String>();
			_map.put(b, _set);
		}
		
		if (!_set.contains(c)){
			_set.add(c);
		}
	}
	
	public Map<String, Set<String>> map(String subject, String predicate, String object) throws GraphException {
		if ((subject==null && predicate==null && object!=null) || (subject==null && predicate!=null && object==null)
				|| (subject!=null && predicate==null && object==null)){
			return (Map<String, Set<String>>)triples(subject, predicate, object);
		}
		else throw new GraphException("To return a map, only 1 parameter must be used!");
	}
	
	public Set<String> list(String subject, String predicate, String object) throws GraphException {
		if ((subject==null && predicate!=null && object!=null) || (subject!=null && predicate==null && object!=null)
				|| (subject!=null && predicate!=null && object==null)){
			return (Set<String>)triples(subject, predicate, object);
		}
		else throw new GraphException("To return a set, exactly 2 parameters must be used!");
	}
	
	public String value(String subject, String predicate, String object) throws GraphException {
		if (subject==null || predicate==null || object==null){
			Set<String> set = (Set<String>) triples(subject, predicate, object);
			if (set.size()==1){
				return (String)set.toArray()[0];
			}
			else throw new GraphException("Result contained more than 1 value, unable to return single value for parameters!");
		}
		else throw new GraphException("To return a value, exactly 2 parameters must be used!");
	}
	
	public Boolean is(String subject, String predicate, String object) throws GraphException {
		if (subject!=null && predicate!=null && object!=null){
			return (Boolean) triples(subject, predicate, object);
		}
		else throw new GraphException("To check if a triple is true, all 3 parameters must be used!");
	}
	
	public Object triples(String subject, String predicate, String object){
		if (subject==null){
			if (predicate==null){ // subject == predicate == null
				if (object==null){
					return _spo;
				}
				else { // subject == null, object != null
					return _osp.get(object);
				}
			}
			else if (object==null){ // subject == null, predicate != null, object == null
				return _pos.get(predicate);
			}
			else { // subject == null, predicate != null, object != null
				return _pos.get(predicate).get(object);
			}
		}
		else if (predicate==null){ // subject != null, predicate == null
			if (object==null){ // subject != null, predicate == null, object == null
				return _spo.get(subject);
			}
			else { // subject != null, predicate == null, object != null
				return _osp.get(object).get(subject);
			}
		}
		else if (object==null) { // subject != null, predicate != null, object == null
			return _spo.get(subject).get(predicate);
		}
		else { // subject != null, predicate != null, object != null
			if (_spo.get(subject)!=null){
				return _spo.get(subject).get(predicate).contains(object);
			}
			else {
				return Boolean.FALSE;
			}
		}
	}
	
	public boolean isSubject(String subject){
		return _spo.containsKey(subject);
	}
	
	public boolean isPredicate(String predicate){
		return _pos.containsKey(predicate);
	}
	
	public boolean isObject(String object){
		return _osp.containsKey(object);
	}
	
	private Map<String, Set<String>> handleHashSet(Map<String, Set<String>> bindings,
			Map<String, String> variables, Set<String> triples) throws GraphException {
		if (variables.size()==1){
			if (bindings==null){
				// first run on bindings, just add every match!
				bindings = new HashMap<String, Set<String>>();
				bindings.put(variables.get(variables.keySet().toArray()[0]), triples);
				// System.out.println("BIND: added everything");
			}
			else {
				Map<String, Set<String>> newBindings = new HashMap<String, Set<String>>();
				Set<String> newSet = new HashSet<String>();
				for (String hit: bindings.get(variables.get(variables.keySet().toArray()[0]))){
					if (triples.contains(hit)){
						newSet.add(hit);
					}
				}
				newBindings.put(variables.get(variables.keySet().toArray()[0]), newSet);
				bindings = newBindings;
				// System.out.println("BIND: updated existing");
			}
			// System.out.println("\t" + bindings);
		}
		else{
			throw new GraphException("Only 1 variable supported! Size is " + variables.size() + " and content: " + variables);
		}
		return bindings;
	}
	
	private Map<String, Set<String>> handleHashMap(Map<String, Set<String>> bindings,
			Map<String, String> variables, Map<String, Set<String>> triples) throws GraphException {
		if (variables.size()==2){
			
			System.out.println("handling result: " + triples);
			
			Map<String, Set<String>> newBindings = new HashMap<String, Set<String>>();

			if (bindings==null){
				newBindings = triples;
			}
			else {
				Map<String, Set<String>> map = triples;
				for (Iterator<String> itr=map.keySet().iterator(); itr.hasNext(); ){ 
					// TODO optimize this, move iteration to a method, call from each if
					if (variables.containsKey("subject") && variables.containsKey("predicate")){
						//OSP (has object, give me subject and predicate)
						String subject = itr.next();
					}
					else if (variables.containsKey("subject") && variables.containsKey("object")){
						//POS (has predicate, give me object and subjects)
						String object = itr.next();
						Set<String> subjects = map.get(object);
						
						if (bindings.containsKey(variables.get("object"))){
							if (bindings.get(variables.get("object")).contains(object)){
								// ?
							}
						}
						else {
							// ?
						}
						
						// Subjects
						if (bindings.containsKey(variables.get("subject"))){
							Set<String> newSubjects = new HashSet<String>();
							for (String oldSubject: bindings.get(variables.get("subject"))){
								if (subjects.contains(oldSubject)){
									// ?
								}
							}
							newBindings.put(variables.get("subject"), newSubjects);
						}
						else {
							newBindings.put(variables.get("subject"), subjects);
						}
						
					}
					else if (variables.containsKey("predicate") && variables.containsKey("object")){
						//SPO (has subject, give me the rest)
						String predicate = itr.next();
					}
					else {
						throw new GraphException("There is something wrong with the variables: " +  variables);
					}
				}
			}
			return newBindings;
		}
		else {
			throw new GraphException("Null is not yet supported for query input parameter");
		}
	}
	
	public Object query(List<String[]> query) throws GraphException {
		Map<String, Set<String>> bindings = null;
		Map<String,String> variables = new HashMap<String,String>();
		
		for (String[] triple: query){
			System.out.println(triple[0] + "," + triple[1] + "," + triple[2]);
			if(triple.length==3){
				String[] qc = new String[3];
				for (int i=0; i<3; i++){
					if(triple[i].startsWith("?")){
						variables.put(i==0?"subject":(i==1?"predicate":"object"), triple[i]);
						qc[i]=null;
					}
					else {
						qc[i] = triple[i];
					}
				}
				Object result = triples(qc[0], qc[1], qc[2]);

				if (result instanceof HashSet){
					bindings = handleHashSet(bindings, variables, (Set<String>)result);
				}
				else if (result instanceof HashMap){
					bindings = handleHashMap(bindings, variables, (Map<String, Set<String>>) result);
				}
				else {
					throw new GraphException("Type not yet supported: " + result.getClass());
				}
			}
			else {
				throw new GraphException("A query triple should have exactly 3 entries: " + triple);
			}
		}
		return bindings;
	}

// --- IMPLEMENTATION END ---
}

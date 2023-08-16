package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;


public class SemanticAnalyzer extends VisitorAdaptor {
	
	int printCount = 0;
	int varDeclCount = 0;
	boolean errorDetected = false;
	
	Struct currentType = null;

	
	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}

	public void visit(VarOneVar oneVar){
		varDeclCount++;
		
		Obj foundVar = Tab.find(oneVar.getName());
    	if(foundVar == Tab.noObj){
    		Obj varNode = Tab.insert(Obj.Var, oneVar.getName(), currentType);
    	}else{
    		report_error("Greska: Ime " + oneVar.getName() + " je vec deklarisano!", oneVar);	
    	}
		
	}
	
	public void visit(VarOneArray arrayVar){
		varDeclCount++;
		
		Obj foundVar = Tab.find(arrayVar.getName());
    	if(foundVar == Tab.noObj){
    		Obj varNode = Tab.insert(Obj.Var, arrayVar.getName(), new Struct(Struct.Array, currentType));
    	}else{
    		report_error("Greska: Ime " + arrayVar.getName() + " je vec deklarisano!", arrayVar);	
    	}
		
	}
	
    public void visit(StatementPrint print) {
		printCount++;
	}

    public void visit(ProgName progName) {
    	progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
    	Tab.openScope();
	}
    
    public void visit(Program program) {
    	Tab.chainLocalSymbols(program.getProgName().obj);
    	Tab.closeScope();
	}
    
    
    public void visit(Type type){
    	Obj typeNode = Tab.find(type.getTypeName());
    	if(typeNode == Tab.noObj){
    		report_error("Greska: Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", null);
    		type.struct = Tab.noType;
    	}else{
    		if(typeNode.getKind() == Obj.Type){
    			type.struct = typeNode.getType();
    			currentType = type.struct;
    		}else{ 
    			report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", type);
    			type.struct = Tab.noType;
    		}
    	}
    }
    
    
    
    
    

}

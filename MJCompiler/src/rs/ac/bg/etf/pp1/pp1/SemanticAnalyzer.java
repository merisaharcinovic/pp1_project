package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;


public class SemanticAnalyzer extends VisitorAdaptor {
	
	int printCount = 0;
	int varDeclCount = 0;
	boolean errorDetected = false;
	boolean hasMain=false;
	
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

	
	
    public void visit(StatementPrint print) {
		printCount++;
	}

    public void visit(ProgName progName) {
    	progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
    	Tab.openScope();
	}
    
    public void visit(Program program) {
    	if(!hasMain) report_error("Greska: Nije pronadjena main metoda. ", null);

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
    
    public void visit(VarOneVar oneVar){
		varDeclCount++;
		
		Obj foundVar = Tab.find(oneVar.getName());
    	if(foundVar == Tab.noObj){
    		Obj varNode = Tab.insert(Obj.Var, oneVar.getName(), currentType);
    		report_info("Deklarisana promenljiva: " + oneVar.getName(), oneVar);
    	}else{
    		report_error("Greska: Ime " + oneVar.getName() + " je vec deklarisano!", oneVar);	
    	}
		
	}
	
	public void visit(VarOneArray arrayVar){
		varDeclCount++;
		
		Obj foundVar = Tab.find(arrayVar.getName());
    	if(foundVar == Tab.noObj){
    		Obj varNode = Tab.insert(Obj.Var, arrayVar.getName(), new Struct(Struct.Array, currentType));
    		report_info("Deklarisana promenljiva: " + arrayVar.getName(), arrayVar);

    	}else{
    		report_error("Greska: Ime " + arrayVar.getName() + " je vec deklarisano!", arrayVar);	
    	}
		
	}
    
	//deklaracija CONST 
	
    public void visit(ConstNumber constNumber) {
    	Obj found = Tab.find(constNumber.getName());
    	
    	if(found == Tab.noObj){
    		if(currentType.getKind() !=  Struct.Int){
        		report_error("Greska: Konstanta " + constNumber.getName() + " nije ispravnog tipa!", constNumber);	
    		}
    		else {
    			//insert obj cvor u tab i postavi vrednost value u adr
        		Obj constNumNode = Tab.insert(Obj.Con, constNumber.getName(), Tab.intType);
        		constNumNode.setAdr(constNumber.getValue());
        		report_info("Deklarisana NUMBER konstanta: " + constNumber.getName() + "= " + constNumber.getValue(), constNumber);

    		}
    	}else{
    		report_error("Greska: Ime " + constNumber.getName() + " je vec deklarisano!", constNumber);	
    	}
	}
    
    
    public void visit(ConstChar constChar) {
    	Obj found = Tab.find(constChar.getName());
    	
    	if(found == Tab.noObj){
    		if(currentType.getKind() !=  Struct.Char){
        		report_error("Greska: Konstanta " + constChar.getName() + " nije ispravnog tipa!", constChar);	
    		}
    		else {
        		Obj constCharNode = Tab.insert(Obj.Con, constChar.getName(), Tab.charType);
        		constCharNode.setAdr(constChar.getValue());
        		report_info("Deklarisana CHAR konstanta: " + constChar.getName() + "= " + constChar.getValue(), constChar);

    		}
    	}else{
    		report_error("Greska: Ime " + constChar.getName() + " je vec deklarisano!", constChar);	
    	}
	}
    
    public void visit(ConstBool constBool) {
    	Obj found = Tab.find(constBool.getName());
    	
    	if(found == Tab.noObj){
    		if(currentType.getKind() !=  Struct.Bool){
        		report_error("Greska: Konstanta " + constBool.getName() + " nije ispravnog tipa!", constBool);	
    		}
    		else {
        		Obj constBoolNode = Tab.insert(Obj.Con, constBool.getName(), Tab.intType);
        		int value = constBool.getValue().equals("true") ? 1 : 0;
        		constBoolNode.setAdr(value);
        		report_info("Deklarisana BOOLEAN konstanta: " + constBool.getName() + "= " + constBool.getValue(), constBool);

    		}
    	}else{
    		report_error("Greska: Ime " + constBool.getName() + " je vec deklarisano!", constBool);	
    	}
	}
    
    
    //methodDecl
    
    public void visit(MethodDecl methodDecl){
		varDeclCount++;
		
		Tab.chainLocalSymbols(methodDecl.getMethodName().obj);
    	Tab.closeScope();
	}
    
    public void visit(MethodName methodName){
		varDeclCount++;
		
		String name = methodName.getName();
    	if(name.equals("main")){
    		hasMain=true;
    		methodName.obj = Tab.insert(Obj.Meth, methodName.getName(), Tab.noType);
        	Tab.openScope();
    		
    	}else{
    		report_error("Greska: Identifikator " + methodName.getName() + " nije main!", methodName);	
    	}
		
	}
    
    
    
    
    
    
    

}

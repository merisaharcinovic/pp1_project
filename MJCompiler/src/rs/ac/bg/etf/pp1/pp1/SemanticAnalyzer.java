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

	Struct boolType;
	
	public SemanticAnalyzer() {
		boolType = Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool)).getType();
	}

	
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

	


    //Program 
    
    public void visit(ProgName progName) {
    	progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
    	Tab.openScope();
	}
    
    public void visit(Program program) {
    	if(!hasMain) report_error("Greska: Nije pronadjena main metoda. ", null);

    	Tab.chainLocalSymbols(program.getProgName().obj);
    	Tab.closeScope();
	}
    
    
    //Type

    public void visit(Type type){
    	
    	Obj typeNode = Tab.find(type.getTypeName());
    	if(typeNode == Tab.noObj){
    		report_error("Greska: Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola ", null);
    		type.struct = Tab.noType;
    	}else{
    		if(typeNode.getKind() == Obj.Type){
    			type.struct = typeNode.getType();
    			currentType = type.struct;
    		}else{ 
    			report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip", type);
    			type.struct = Tab.noType;
    		}
    	}
    }
    
    //VarOne
    
    public void visit(VarOneVar oneVar){
		if(currentType==null) return;
		
		Obj foundVar = Tab.find(oneVar.getName());
    	if(foundVar == Tab.noObj){
    		Obj varNode = Tab.insert(Obj.Var, oneVar.getName(), currentType);
    		report_info("Deklarisana promenljiva: " + oneVar.getName(), oneVar);
    		varDeclCount++;
    	}else{
    		report_error("Greska: Ime " + oneVar.getName() + " je vec deklarisano", oneVar);	
    	}
		
	}
	
	public void visit(VarOneArray arrayVar){
		if(currentType==null) return;
		
		Obj foundVar = Tab.find(arrayVar.getName());
    	if(foundVar == Tab.noObj){
    		Obj varNode = Tab.insert(Obj.Var, arrayVar.getName(), new Struct(Struct.Array, currentType));
    		report_info("Deklarisana promenljiva: " + arrayVar.getName(), arrayVar);
    		varDeclCount++;

    	}else{
    		report_error("Greska: Ime " + arrayVar.getName() + " je vec deklarisano", arrayVar);	
    	}
		
	}
	
	public void visit(VarDeclaration varDeclaration){
	        currentType = null;
	}
    
	//ConstDeclOne
	
    public void visit(ConstNumber constNumber) {
    	Obj found = Tab.find(constNumber.getName());
    	
    	if(found == Tab.noObj){
    		if(currentType.getKind() !=  Struct.Int){
        		report_error("Greska: Konstanta " + constNumber.getName() + " nije ispravnog tipa", constNumber);	
    		}
    		else {
    			//insert obj cvor u tab i postavi vrednost value u adr
        		Obj constNumNode = Tab.insert(Obj.Con, constNumber.getName(), Tab.intType);
        		constNumNode.setAdr(constNumber.getValue());
        		report_info("Deklarisana int konstanta: " + constNumber.getName() + "= " + constNumber.getValue(), constNumber);

    		}
    	}else{
    		report_error("Greska: Ime " + constNumber.getName() + " je vec deklarisano", constNumber);	
    	}
	}
    
    
    public void visit(ConstChar constChar) {
    	Obj found = Tab.find(constChar.getName());
    	
    	if(found == Tab.noObj){
    		if(currentType.getKind() !=  Struct.Char){
        		report_error("Greska: Konstanta " + constChar.getName() + " nije ispravnog tipa", constChar);	
    		}
    		else {
        		Obj constCharNode = Tab.insert(Obj.Con, constChar.getName(), Tab.charType);
        		constCharNode.setAdr(constChar.getValue());
        		report_info("Deklarisana char konstanta: " + constChar.getName() + "= " + constChar.getValue(), constChar);

    		}
    	}else{
    		report_error("Greska: Ime " + constChar.getName() + " je vec deklarisano", constChar);	
    	}
	}
    
    public void visit(ConstBool constBool) {
    	Obj found = Tab.find(constBool.getName());
    	
    	if(found == Tab.noObj){
    		if(currentType.getKind() !=  Struct.Bool){
        		report_error("Greska: Konstanta " + constBool.getName() + " nije ispravnog tipa", constBool);	
    		}
    		else {
        		Obj constBoolNode = Tab.insert(Obj.Con, constBool.getName(), Tab.intType);
        		int value = constBool.getValue().equals(true) ? 1 : 0;
        		constBoolNode.setAdr(value);
        		report_info("Deklarisana boolean konstanta: " + constBool.getName() + "= " + constBool.getValue(), constBool);

    		}
    	}else{
    		report_error("Greska: Ime " + constBool.getName() + " je vec deklarisano", constBool);	
    	}
	}
    
    
    //MethodDecl
    
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
    		report_error("Greska: Identifikator " + methodName.getName() + " nije main", methodName);	
    	}
		
	}
    
    
    //Designator
    
    public void visit(DesignatorOne designatorOne){
    	Obj found = Tab.find(designatorOne.getName());
    	
    	if(found == Tab.noObj) {
    		report_error("Greska: Ime " + designatorOne.getName() + " nije deklarisano", designatorOne);
    		designatorOne.obj=Tab.noObj;

    	}
    	else {

    		report_info("Detektovano koriscenje promenljive " + designatorOne.getName(), designatorOne);
    		designatorOne.obj= found;
    	}
    	
    }
    
    public void visit(DesignatorExpr designatorExpr){
    	Obj designator = designatorExpr.getDesignator().obj;
    	Obj expr = designatorExpr.getExpr().obj;

    	//provera da li je designator tipa niz
    	if(designator.getType().getKind()!=Struct.Array ) {
    		report_error("Greska: Identifikator " + designator.getName() + " nije tipa niz", designatorExpr);	
    		designatorExpr.obj=Tab.noObj;
    	}
		//provera da li je expr int
    	else if(expr.getType().getKind()!=Struct.Int) {
    		report_error("Greska: Izraz unutar [] nije tipa int", designatorExpr);	
    		designatorExpr.obj=Tab.noObj;
    	}
    	else {
    		report_info("Detektovano koriscenje promenljive " + designator.getName(), designatorExpr);
    		designatorExpr.obj = new Obj(Obj.Elem, "", designator.getType().getElemType());
            
    	}
    	
	}
    
    //DesignatorStatement
    
    
    public void visit(DesignatorInc designatorInc){
    	
    	Obj designator = designatorInc.getDesignator().obj;
    	boolean error=false;

    	
    	if(!isInt(designator.getType())) {
    		report_error("Greska: Operand koji se inkrementira mora biti tipa int", designatorInc);
    		error=true;
    	}
    	if(designator.getKind()!=Obj.Elem && designator.getKind()!=Obj.Var) {
    		report_error("Greska: Operand koji se inkrementira mora oznacavati promenljivu ili element niza", designatorInc);	
    		error=true;
    	}
    	if(!error) {
    		report_info("Inkrementiranje promenljive: " + designator.getName(), designatorInc);
        
    	}
    	
    	
    }
    
    
    public void visit(DesignatorDec designatorDec){
    	
    	Obj designator = designatorDec.getDesignator().obj;
    	boolean error=false;
    	
    	if(!isInt(designator.getType())) {
    		error=true;
    		report_error("Greska: Operand koji se dekrementira mora biti tipa int", designatorDec);
		}

    	if(designator.getKind()!=Obj.Elem && designator.getKind()!=Obj.Var) {
    		error=true;
    		report_error("Greska: Operand koji se dekrementira mora oznacavati promenljivu ili element niza", designatorDec);	
    	}
    	if(!error) {
    		report_info("Dekrementiranje promenljive: " + designator.getName(), designatorDec);
        
    	}
    	
    }
    
    
    public void visit(DesignatorAssign designatorAssign){
    	
    	Obj designator = designatorAssign.getDesignator().obj;
    	Obj expr = designatorAssign.getExpr().obj;

    	//Expr type kompatibilan sa designator type
    	if(!expr.getType().assignableTo(designator.getType())) {
    		report_error("Greska: Tip izraza mora biti kompatibilan pri dodeli tipu sa leve strane jednakosti", designatorAssign);	
    	}
    	
    	if(!(designator.getKind()==Obj.Elem || designator.getKind()==Obj.Var)) {
    		report_error("Greska: Na levoj strani jednakosti mora biti promenljiva ili element niza", designatorAssign);	

    	}
    
    }


	//Term
    
    public void visit(TermFactor term){
    	term.obj = term.getFactor().obj;
    	
    }
    
    public void visit(TermMul termMul){
    	Struct factorType = termMul.getFactor().obj.getType();
        Struct termType = termMul.getTerm().obj.getType();
        
        if(compatibleInts(factorType, termType)) {
        	termMul.obj=termMul.getTerm().obj;
        }
        else {
    		report_error("Greska: Tipovi u izrazu nisu kompatibilni ", termMul);	
    		termMul.obj=new Obj(Obj.NO_VALUE, null, Tab.noType );
        }
        
    }
    
    //Factor
    
    public void visit(FactorNewExpr factorNew){
    	
    	Obj expr = factorNew.getExpr().obj;
    	Struct type = factorNew.getType().struct;
    	
    	if(!isInt(expr.getType())) {
    		report_error("Greska: Izraz unutar [] mora biti tipa int", factorNew);	
    		factorNew.obj=new Obj(Obj.NO_VALUE, null, Tab.noType );
    	}
    	else {
    		factorNew.obj = new Obj(Obj.Con, "", new Struct(Struct.Array, type));
    		//report_info("TEST FACTOR NEW EXPR", factorNew);
    	}
    	
    }
    
    public void visit(FactorDesignator factorDesignator){
    	factorDesignator.obj=factorDesignator.getDesignator().obj;
    	
    }
    
    public void visit(FactorNumber factorNumber){
    	factorNumber.obj = new Obj(Obj.Con, "", Tab.intType, factorNumber.getValue(), 1);
    }
    public void visit(FactorChar factorChar){
    	factorChar.obj = new Obj(Obj.Con, "", Tab.charType, factorChar.getValue(), 1);
    }
	public void visit(FactorBool factorBool){	
    	factorBool.obj = new Obj(Obj.Con, "", boolType, factorBool.getValue()?1:0, 1);
	}
	
	public void visit(FactorExpr factorExpr){
		factorExpr.obj=factorExpr.getExpr().obj;
	}
	    
    //Statement
	
	 public void visit(StatementFindAny findAny){
    	Struct designator1=findAny.getDesignator().obj.getType();
    	Struct designator2=findAny.getDesignator1().obj.getType();
    	Struct expr = findAny.getExpr().obj.getType();
    	
    	boolean error=false;
    	
    	if(designator2.getKind()!=Struct.Array) {
    		report_error("Greska: Identifikator sa desne strane jednakosti mora oznacavati niz ugradjenog tipa", findAny);	
    		error=true;
    	}
    	if(designator1.getKind()!=Struct.Bool) {
    		report_error("Greska: Identifikator sa leve strane jednakosti mora biti tipa boolean ", findAny);	
    		error=true;
    	}
    	if(!error) {
            report_info("Detektovan poziv funkcije findAny. ", findAny);

    	}
    	
	 }
	
    
    public void visit(StatementRead readStmt){
    	
    	boolean error=false;
    	Obj designator = readStmt.getDesignator().obj;
    	if(designator.getKind()!=Obj.Elem && designator.getKind()!=Obj.Var) {
    		report_error("Greska: Element unutar read() mora oznacavati promenljivu ili element niza", readStmt);	
    		error=true;
    	}
    	if(!isInt(designator.getType()) &&!isChar(designator.getType()) && !isBool(designator.getType())   ) {
    		report_error("Greska: Element unutar read() mora biti tipa int, char ili bool", readStmt);
    		error = true;
    	}
    	if(!error) {
            report_info("Detektovan poziv funkcije read ", readStmt);

    	}
    	
    	
    }
    
    public void visit(StatementPrint printStmt){
    	printCount++;
    	
    	Obj expr = printStmt.getExpr().obj;
    	if(!isInt(expr.getType()) && !isChar(expr.getType()) && !isBool(expr.getType())) {
    		report_error("Greska: Izraz unutar print() mora biti tipa int, char ili bool", printStmt);	
    	}
    	else {
            report_info("Detektovan poziv funkcije print ", printStmt);
    	}
    	
    }
    
    //Expr
    
    public void visit(ExprNegative expr){
    	Struct termType = expr.getTerm().obj.getType();
    	if(!isInt(termType)) {
    		report_error("Greska: Negativni clan mora biti tipa int", expr);	
    		expr.obj=new Obj(Obj.NO_VALUE, null, Tab.noType );
    	}
    	else {
    		expr.obj=expr.getTerm().obj;
    	}
    }
    
    public void visit(ExprPositive expr){
    	expr.obj=expr.getTerm().obj;
    }
    
    
    public void visit(ExprAdd exprAdd){
    	Struct exprType = exprAdd.getExpr().obj.getType();
        Struct termType = exprAdd.getTerm().obj.getType();
        
        
        if(!exprType.compatibleWith(termType)) {
        	report_error("Greska: Tipovi u izrazu nisu kompatibilni", exprAdd);	
    		exprAdd.obj=new Obj(Obj.NO_VALUE, null, Tab.noType );        
    	}
       
        else if(compatibleInts(exprType, termType)) {
        	exprAdd.obj=exprAdd.getExpr().obj;
        }
        else {
        	report_error("Greska: Tipovi u izrazu moraju biti int", exprAdd);	
    		exprAdd.obj=new Obj(Obj.NO_VALUE, null, Tab.noType );       
        }
        
        
    }
    
    
    
    
    public boolean compatibleInts(Struct str1, Struct str2){
    	return (isInt(str1) && isInt(str2));
    }

	private boolean isInt(Struct str1) {
		return (str1.getKind()==Struct.Int );
				//|| (str1.getKind()==Struct.Array && str1.getElemType().getKind()==Struct.Int));
	}
	
	private boolean isChar(Struct str1) {
		return (str1.getKind()==Struct.Char );
				//|| (str1.getKind()==Struct.Array && str1.getElemType().getKind()==Struct.Char));
	}
	private boolean isBool(Struct str1) {
		return (str1.getKind()==Struct.Bool );
				//|| (str1.getKind()==Struct.Array && str1.getElemType().getKind()==Struct.Bool));
	}
    

}

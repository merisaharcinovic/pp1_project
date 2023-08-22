package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;
	Struct boolType;
	
	public int getMainPc(){
		return mainPc;
	}
	
	public CodeGenerator() {
		boolType = Tab.find("bool").getType();
	}
	
	public void visit(StatementPrint printStmt){
		if(printStmt.getExpr().obj.getType() == Tab.intType){
			Code.loadConst(5);
			Code.put(Code.print);
		}else{
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}
	
	public void visit(StatementRead readStmt){
		Obj designator = readStmt.getDesignator().obj;
        if (designator.getType() == Tab.intType) {
            Code.put(Code.read);
        }
        else {
            Code.put(Code.bread);
        }
        
        if(designator.getKind() == Obj.Elem) {
        	if(designator.getType() == Tab.intType || designator.getType() == boolType) {
        		Code.put(Code.astore);
        	}
        	else {
                Code.put(Code.bastore);
        	}
        }
        else {
        	Code.store(designator);
        }
    }
	public void visit(NumConst numConst){
	}
	
	
	public void visit(MethodName methodName){
		
		String name = methodName.getName();
    	if(name.equals("main")){
    		mainPc = Code.pc;
    	}
    	
    	methodName.obj.setAdr(Code.pc);
		SyntaxNode methodNode = methodName.getParent();
	
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);
		
		Code.put(Code.enter);
		Code.put(0);
		Code.put(0 + varCnt.getCount());
		
	}
	
	public void visit(MethodDecl methodDecl){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ExprAdd addExpr){
		if(addExpr.getAddOp().getClass()==Plus.class) {
			Code.put(Code.add);
		}
		else {
			Code.put(Code.sub);
		}
		
	}

	public void visit(ExprNegative expr){
		Code.put(Code.neg);
	}
	
	public void visit(TermMul termMul) {
		if(termMul.getMulOp().getClass() == Mul.class){
            Code.put(Code.mul);
        }
		else if(termMul.getMulOp().getClass() == Div.class){
            Code.put(Code.div);
        }
		else if(termMul.getMulOp().getClass() == Mod.class){
            Code.put(Code.rem);
        }
	}
	
	public void visit(FactorNumber factorNumber){
        Code.load(factorNumber.obj);
	}
    public void visit(FactorChar factorChar){
    	Code.load(factorChar.obj);
    }
	public void visit(FactorBool factorBool){	
    	Code.load(factorBool.obj);
	}

	public void visit(DesignatorAssign assignment){
		Code.store(assignment.getDesignator().obj);
	}
	
	public void visit(FactorNewExpr factorNew){
        Code.put(Code.newarray);
        if (factorNew.getType().struct == Tab.intType) {
            Code.put(1);
        } else {
            Code.put(0);
        }
    }
	
	
	
}

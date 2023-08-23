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
		if(printStmt.getExpr().obj.getType() == Tab.intType || printStmt.getExpr().obj.getType() == boolType){
			if(printStmt.getNumConstOpt().getClass()==NoNumConst.class) {
				Code.loadConst(5);
			}
			Code.put(Code.print);
		}else{
			if(printStmt.getNumConstOpt().getClass()==NoNumConst.class) {
				Code.loadConst(1);
			}
			Code.put(Code.bprint);
		}
	}
	
	public void visit(NumConst numConst){
		Code.loadConst(numConst.getWidth());
	}
	
	
	public void visit(StatementRead readStmt){
		Obj designator = readStmt.getDesignator().obj;
        if (designator.getType() == Tab.intType || designator.getType() == boolType) {
            Code.put(Code.read);
        }
        else {
            Code.put(Code.bread);
        }
        
        Code.store(designator);
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
	
	public void visit(DesignatorOne designatorOne){
		if(designatorOne.getParent().getClass()!=DesignatorAssign.class 
				&& designatorOne.getParent().getClass()!=DesignatorInc.class
				&& designatorOne.getParent().getClass()!=DesignatorDec.class
				&& designatorOne.getParent().getClass()!=StatementRead.class) {
			Code.load(designatorOne.obj);

		}
	}
	
	public void visit(DesignatorExpr designatorExpr){
		if(designatorExpr.getParent().getClass()!=DesignatorAssign.class 
				&& designatorExpr.getParent().getClass()!=DesignatorInc.class
				&& designatorExpr.getParent().getClass()!=DesignatorDec.class
				&& designatorExpr.getParent().getClass()!=StatementRead.class) {
			Code.load(designatorExpr.obj);

		}
    }
	
	public void visit(DesignatorInc designatorInc){
		
		Obj designator = designatorInc.getDesignator().obj;
		if (designator.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		//designator
		Code.load(designator);
		//1
		Code.loadConst(1);
		//add
        Code.put(Code.add);
        //store
        Code.store(designator);
		
		
	}
	
	public void visit(DesignatorDec designatorDec){
		Obj designator = designatorDec.getDesignator().obj;
		if (designator.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		//designator
		Code.load(designator);
		//1
		Code.loadConst(1);
		//sub
        Code.put(Code.sub);
        //store
        Code.store(designator);
	}
	

	public void visit(FactorNewExpr factorNew){
        Code.put(Code.newarray);
        if (factorNew.getType().struct == Tab.intType || factorNew.getType().struct == boolType) {
            Code.put(1);
        } else {
            Code.put(0);
        }
    }
	
	public void visit(StatementFindAny findAny){
		Obj designator=findAny.getDesignator1().obj;
		
		//b = niz.findAny(5)
		//b, niz, 5
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		
		
		Code.put(Code.dup_x1);
		Code.put(Code.dup);

		//b, niz, 5, niz, niz
		
		Code.put(Code.arraylength);
		Code.loadConst(1);
		Code.put(Code.sub);

		//b, niz, 5, niz, len-1
		
		
		
		//------WHILE(i>-1)----------
		
		int whileStart=Code.pc;
		
		Code.put(Code.dup);
		Code.loadConst(-1);
		
		//b, niz, 5, niz, len-1, len-1, -1
		
		int end_not_found = Code.pc +1;
		Code.putFalseJump(Code.ne, 0);
		
		//b, niz, 5, niz, len-1
		
		Code.put(Code.dup);
		Code.loadConst(1);
		Code.put(Code.sub);
		
		//b, niz, 5, niz, len-1, len-2
		
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		
		//b, niz, 5, len-2, niz, len-1
		
		if(designator.getType().getKind()==Struct.Char) Code.put(Code.baload);
		else Code.put(Code.aload);
		
		//b, niz, 5, len-2, niz[len-1]
		
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		
		//b, niz, niz[len-1], 5, len-2
		
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		
		//b, niz, len-2, niz[len-1], 5
		Code.put(Code.dup_x2);
		
		//b, niz, 5, len-2, niz[len-1], 5
		
		int set_stack = Code.pc +1;
		Code.putFalseJump(Code.eq, 0);
		
		//-----FOUND-------
		
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.put(Code.pop);

		Code.loadConst(1);
		
		//1
		//STORE B=1;
		Code.store(findAny.getDesignator().obj);
		Code.loadConst(1);
		Code.loadConst(1);
		
		int end = Code.pc +1;
		Code.putFalseJump(Code.ne, 0);

		//-----------------
		
		
		
		//--------SET_STACK------
		
		Code.fixup(set_stack);
		
		//b, niz, 5, len-2
		
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		
		//b, 5, len-2, niz
		
		Code.put(Code.dup_x2);
		//b, niz, 5, len-2, niz
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		
		//b, niz, 5, niz, len-2

		Code.putJump(whileStart);
		
		//-----------------------
		
		
		
		//----NOT FOUND----
		Code.fixup(end_not_found);

		
		//b, niz, 5, niz, len-1
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.put(Code.pop);


		Code.loadConst(0);
		//STORE B=0;
		Code.store(findAny.getDesignator().obj);
		
		Code.fixup(end);
		
    }
	
	
	
}

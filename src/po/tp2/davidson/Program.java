package po.tp2.davidson;

import gurobi.*;

public class Program 
{
        public static void main(String[] args) 
    {
        try
        {
            GRBEnv enviroment = new GRBEnv("po-tp2.log");
            GRBModel model = new GRBModel(enviroment);
            GRBVar x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "X");
            GRBVar y = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "Y");
            
            GRBLinExpr expression = new GRBLinExpr();
            expression.addTerm(5.0, x);
            expression.addTerm(4.0, y);
            model.setObjective(expression, GRB.MAXIMIZE);
            
            expression = new GRBLinExpr();
            expression.addTerm(1.0, x);
            expression.addTerm(1.0, y);
            model.addConstr(expression, GRB.LESS_EQUAL, 5.0, "C1");
            
            expression = new GRBLinExpr();
            expression.addTerm(10.0, x);
            expression.addTerm(6.0, y);
            model.addConstr(expression, GRB.LESS_EQUAL, 45.0, "C1");
       
            BranchBound branchBound = new BranchBound(model);
        }
        catch(GRBException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

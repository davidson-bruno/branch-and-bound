package po.tp2.davidson;

import gurobi.*;
import java.io.*;

public class BranchBound 
{
    private GRBModel initialModel;
    private Double[] varsValue;
    private Double bestFOValue;
    private BufferedWriter bw;
    private int level;
    
    public BranchBound(GRBModel initialModel) throws GRBException
    {
        File logFile = new File("log/branch-bound.log");
        
        if(!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        
        try
        {
            FileWriter fw = new FileWriter(logFile);
            bw = new BufferedWriter(fw);
            
            initialModel.optimize();
        
            this.initialModel = initialModel;
            varsValue = new Double[initialModel.getVars().length];

            for(int i = 0; i < varsValue.length; ++i)
            {
                varsValue[i] = 0.0;
            }

            this.bestFOValue = 0.0;
            this.level = 0;
            solve(initialModel);
            bw.newLine();
            showResults();
            
            bw.close();
            fw.close();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void solve(GRBModel model) throws GRBException, IOException
    {
        bw.write("Entrou no nível " + level + " da árvore.");
        bw.newLine();
        
        model.optimize();
        
        int status = model.get(GRB.IntAttr.Status);
        
        if(status != GRB.OPTIMAL)
        {
            return;
        }
        
        for(int i = 0; i < model.getVars().length; ++i)
        {
            GRBVar var = model.getVar(i);
            
            bw.write(var.get(GRB.StringAttr.VarName) + ": " + var.get(GRB.DoubleAttr.X));
            bw.newLine();
        }
        
        bw.write("FO: " + model.get(GRB.DoubleAttr.ObjVal));
        bw.newLine();
        
        GRBVar[] vars = model.getVars();
        Boolean[] isInteger = new Boolean[vars.length];
        Boolean allAreIntegers = true;
        
        for(int i = 0; i < vars.length; ++i)
        {
            isInteger[i] = vars[i].get(GRB.DoubleAttr.X) % 1.0 == 0;
        }
        
        for(int i = 0; i < vars.length; ++i)
        {
            if(!isInteger[i])
            {
                allAreIntegers = false;
                break;
            }
        }
        
        if(allAreIntegers)
        {
            if(model.get(GRB.DoubleAttr.ObjVal) > bestFOValue)
            {
                for(int i = 0; i < vars.length; ++i)
                {
                    varsValue[i] = vars[i].get(GRB.DoubleAttr.X);
                }
                
                bestFOValue = model.get(GRB.DoubleAttr.ObjVal);
                bw.write("Solução encontrada!");
                bw.newLine();
                bw.write("Saiu do nível " + level + " da árvore.");
                bw.newLine();
                level--;
            }
        }
        else
        {
            bw.newLine();
            for(int i = 0; i < vars.length; ++i)
            {
                if(!isInteger[i])
                {
                    GRBModel floorModel = new GRBModel(model);
                    GRBModel ceilModel = new GRBModel(model);

                    GRBLinExpr expression = new GRBLinExpr();
                    expression.addTerm(1.0, vars[i]);

                    GRBConstr floor = floorModel.addConstr(expression, GRB.LESS_EQUAL, Math.floor(vars[i].get(GRB.DoubleAttr.X)), "Floor");
                    level++;
                    solve(floorModel);
                    level--;
                    
                    GRBConstr ceil = ceilModel.addConstr(expression, GRB.GREATER_EQUAL, Math.ceil(vars[i].get(GRB.DoubleAttr.X)), "Ceil");
                    level++;
                    solve(ceilModel);
                    level--;
                }
            }
        }
        bw.newLine();
    }
    
    public void showResults() throws GRBException, IOException
    {
        bw.write("Melhor solução encontrada:");
        bw.newLine();
        
        for(int i = 0; i < this.varsValue.length; ++i)
        {
            bw.write(this.initialModel.getVar(i).get(GRB.StringAttr.VarName) + ": " + varsValue[i]);
            bw.newLine();
        }
        
        bw.write("FO: " + bestFOValue);
    }

    public Double getVarValue(int index)
    {
        return varsValue[index];
    }

    public Double getBestFOValue()
    {
        return bestFOValue;
    }
}

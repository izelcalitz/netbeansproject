/*
    A line for each item showing:
id
description
quantity
unit price excluding VAT
total price excluding VAT
total VAT
total price including VAT
    A summary at the end showing:
total price excluding VAT
total VAT
total price including VAT
 */
package izellejavaproject;

import java.io.*;
import java.util.*;

/**
 *
 * @author izelcalitz
 */
public class IzelleJavaProject
{
    ProjectMap pj;
    public IzelleJavaProject()
    {
        pj = new ProjectMap();
        try
        {
            String row;
            //<editor-fold defaultstate="collapsed" desc="init Product">
            BufferedReader csvReader = new BufferedReader(new FileReader("files/items.csv"));
            while ((row = csvReader.readLine()) != null)
            {
                String[] data = row.split(",", 2);           // row cut into 2 parts
                try
                {
                    Product p = new Product(Integer.parseInt(data[0]), data[1]);
                    pj.put(Integer.parseInt(data[0]), p);
                } catch (NumberFormatException n){}
            }//</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="load prices">
            //load prices
            csvReader = new BufferedReader(new FileReader("files/prices.csv"));
            while ((row = csvReader.readLine()) != null)
            {
                String[] data = row.split(",", 2);           // row cut into 2 parts
                try
                {
                    int indexid = Integer.parseInt(data[0]);
                    Double d = Double.parseDouble(data[1]);
                    
                    Product partialProduct = pj.get(indexid);
                    partialProduct.setPrice(d);
                    pj.replace(indexid, partialProduct);
                    
                } catch (NumberFormatException n){}
            }//</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="populate all quantities">
            csvReader = new BufferedReader(new FileReader("files/quantities.csv"));
            while ((row = csvReader.readLine()) != null)
            {
                String[] data = row.split(",", 2);           // row cut into 2 parts
                try
                {
                    int indexid = Integer.parseInt(data[0]);
                    int qty = Integer.parseInt(data[1]);
                    
                    Product partialProduct = pj.get(indexid);
                    partialProduct.setQuantity(qty);
                    partialProduct.finallize();
                    
                    pj.addToTotal(partialProduct.getDouble());
                    pj.replace(indexid, partialProduct);
                    
                } catch (NumberFormatException n){} //header skiep
            }//</editor-fold>
            csvReader.close();
        } catch (FileNotFoundException fileNotFoundException)
        {   System.err.println("file not found");
        } catch (IOException ioException)
        {   System.err.println("ioException");
        }
        //<editor-fold defaultstate="collapsed" desc="write2file">
        try
        {
            File fw = new File("files/izelleoutput.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(fw));
            bw.write(pj.printHeader());
            bw.write(pj.toString());
            bw.write(pj.printSummaryHead());
            bw.write(pj.printSummaryData());
            bw.flush();
            bw.newLine();
            bw.close();
        } catch (IOException ex)
        {
            System.err.println(ex.toString());
        }
//</editor-fold>
    }

    public static void main(String[] args)
    {
        new IzelleJavaProject();
        System.out.println("Thank you.");
    }
}

/** individual product is created for each unique csv row*/
//<editor-fold defaultstate="collapsed" desc="class Product()">
class Product
{
    private final Integer id;
    private final String description;
    private int quantity = 0;
    private Double price = 0.00;
    private Double totalexclvat = 0.00;
    
    //<editor-fold defaultstate="collapsed" desc="init from csv values">
    
    Product(Integer idd, String s)
    {
        this.id = idd;
        this.description = s;
    }
    
    public void setPrice(Double price)
    {
        this.price = price;
    }
    
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
    
//</editor-fold>
    
    //The Product With Data
    public Product()
    {
        this.id = null;
        this.description = null;
    }
    
    // to populate pricing values
    public void finallize()
    {
        this.totalexclvat = this.price * this.quantity;
    }
    
    //only for sum of finalTotalValue
    public Double getDouble(){
        return this.totalexclvat;
    }
    
    /**
     * Product String as it should be displayed in output :
     * e.g. 2873,"Cable, Stranded 12 Core 100m (W)",2,22.38,44.76,6.71,51.47
     *
     * @return formattedString;
     */
    @Override
    public String toString()
    {
        String s = this.id + "," + this.description + ","
                + this.quantity + "," + this.price+","
                + String.format("%.2f",this.totalexclvat) + ","
                + String.format("%.2f",this.totalexclvat * 0.15) + ","
                + String.format("%.2f",this.totalexclvat * 1.15);
        return s;
    }
    
}
//</editor-fold>

/**
 * List of all products
 * 
 * "id","description","quantity","unit-price",
 * "totalexclvat","total-vat","totalinclvat"
 * 
 */
class ProjectMap extends TreeMap<Integer, Product> 
{
    //<editor-fold defaultstate="collapsed" desc="Lines to print to csv">
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        for(Product p : this.values()){
            sb.append(p.toString()).append("\n");}
        return sb.toString();
    }
    
    public String printSummaryHead()
    {
//        return ",,,,\"totalexclvat\",\"total-vat\",\"totalinclvat\"\n";
        return "\n\"totalexclvat\",\"total-vat\",\"totalinclvat\"\n";
    }
    
    public String printSummaryData()
    {
//        return ",,,,"+putAddingTotal+","+(putAddingTotal*0.15)+","+(putAddingTotal*1.15);
        
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("%.2f",putAddingTotal)     ).append(",");
         sb.append(String.format("%.2f",putAddingTotal*0.15)).append(",");
         sb.append(String.format("%.2f",putAddingTotal*1.15));
        return sb.toString();
    }
    
    public String printHeader()
    {
        return "\"id\",\"description\",\"quantity\",\"unit-price\",\"totalexclvat\",\"total-vat\",\"totalinclvat\"\n";
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="sum prices">
    private Double putAddingTotal = 0.00;
    public void addToTotal(Double d)
    {
        this.putAddingTotal = this.putAddingTotal + d;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="@Deprecated">
    @Deprecated
    public Double sumPricesLoop()
    {
    Double loopAddingTotal = 0.00;
        for(Product p : this.values()){
            loopAddingTotal = loopAddingTotal + p.getDouble();}
        System.out.println("showSumOfAllPrices (mapLoop)= " + loopAddingTotal);
        return loopAddingTotal;
    }
     
    @Deprecated
    void showSumOfAllPrices()
    {
        System.out.println("showSumOfAllPrices (put)= " + this.putAddingTotal);
    }
//</editor-fold>
}
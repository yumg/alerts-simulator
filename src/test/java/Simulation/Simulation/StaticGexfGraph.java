package Simulation.Simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;

import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;


public class StaticGexfGraph {

    public static void main(String[] args) {
        Gexf gexf = new GexfImpl();
        Calendar date = Calendar.getInstance();
        
        gexf.getMetadata()
            .setLastModified(date.getTime())
            .setCreator("YuMG")
            .setDescription("Event Source Network");
        gexf.setVisualization(true);

        Graph graph = gexf.getGraph();
        graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
        
        AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
        graph.getAttributeLists().add(attrList);
        
        Attribute attUrl = attrList.createAttribute("class", AttributeType.INTEGER, "Class");
        Attribute attIndegree = attrList.createAttribute("pageranks", AttributeType.DOUBLE, "PageRank");

     
        
        Node gephi = graph.createNode("0");
        gephi
            .setLabel("郝大通")
            .getAttributeValues()
                .addValue(attUrl, "3")
                .addValue(attIndegree, "0.14658");

        
        Node webatlas = graph.createNode("1");
        webatlas
            .setLabel("郝大通")
                .getAttributeValues()
                .addValue(attUrl, "3")
                .addValue(attIndegree, "0.14658");

        gephi.connectTo("0", webatlas).setWeight(0.8f);

        StaxGraphWriter graphWriter = new StaxGraphWriter();
        File f = new File("static_graph_sample.gexf");
        Writer out;
        try {
            out =  new FileWriter(f, false);
            graphWriter.writeToStream(gexf, out, "UTF-8");
            System.out.println(f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}


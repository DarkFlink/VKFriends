package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

import com.mxgraph.layout.*;
import com.mxgraph.swing.*;
import com.mxgraph.view.*;
import com.mxgraph.model.*;

import VKClient.VKClient;
import VKClient.VKUser;

public class MainWindow extends JFrame{
    private final int leftPanelWidth = 300;
    private final int colNum = 20;
    private JPanel toolBar;
    private JPanel buttonBar;
    private JPanel LeftPanel;
    private final Color colorForTools = new Color(55,55,55);
    private final Color colorForOther = new Color(200,200,200);
    private JList<String> mList;
    private DefaultListModel<VKUser> listModel;
    private JTextField InputField;

    private mxGraph graph;
    private mxIGraphLayout layout;

    private VKClient mClient;
    private ArrayList<VKUser> mUsers;
    Integer count = 2;
    protected mxGraphComponent makeGraph(){
        graph = new mxGraph();
        graph.setCellsMovable(false);
        graph.setCellsResizable(false);
        layout = new mxOrganicLayout(graph);
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        return graphComponent;
    }

    protected void makeToolbar(){
        toolBar = new JPanel();
        getContentPane().add(LeftPanel,BorderLayout.WEST);
        toolBar.setLayout( new BorderLayout());
        toolBar.setBackground(colorForTools);

        buttonBar = new JPanel();
        buttonBar.setLayout(new FlowLayout());
        buttonBar.setBackground(colorForTools);

        ImageIcon delImg = new ImageIcon(System.getProperty("user.dir")+"/assets/Icons/erase.png");
        JButton DelButton = new JButton("",delImg);

        ImageIcon resImg = new ImageIcon(System.getProperty("user.dir")+"/assets/Icons/graphreset.png");
        JButton DeleteAll = new JButton( "",resImg);

        DelButton.setBackground(colorForTools);
        DelButton.setEnabled(false);
        DelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String userId = InputField.getText();
                InputField.setText("");
                delVertex(userId);
                count--;
                if(count == 2)
                {
                    DelButton.setEnabled(false);
                    DeleteAll.setEnabled(false);
                }
                // TODO: remove vertex and edges
            }
        });

        DeleteAll.setBackground(colorForTools);
        DeleteAll.setEnabled(false);
        DeleteAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                graph.getModel().beginUpdate();
                graph.setCellsMovable(true);
                Object[] arr=graph.getChildVertices(graph.getDefaultParent());

                for (Object c: arr)
                {
                    mxCell vertex=(mxCell)c;
                    graph.removeCells(graph.getEdges(vertex));
                    graph.getModel().remove(vertex);
                }
                graph.getModel().endUpdate();
                listModel.removeAllElements();
                DeleteAll.setEnabled(false);
                DelButton.setEnabled(false);
                count = 2;
            }
        });

        ImageIcon addImg = new ImageIcon(System.getProperty("user.dir")+"/assets/Icons/add.png");
        JButton AddButton = new JButton("", addImg);
        AddButton.setBackground(colorForTools);
        AddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String userId = InputField.getText();
                InputField.setText("");
                VKUser user = null;

                if(userId.equals("1"))
                {
                    getWarningMassage("Magic Durov account." + ((Integer)listModel.size()).toString());
                    return;
                }
                try {
                    user = mClient.getUser(userId, VKClient.basicArgs);
                }catch (Exception e){
                    getWarningMassage(e.getMessage() + ((Integer)listModel.size()).toString());
                    return;
                }
                if(user == null) {
                    getWarningMassage( ((Integer)listModel.size()).toString());
                    return;
                }
                int[] arrId= new int[listModel.size()];
                for(int i=0;i<listModel.size();i++){
                    if(user.equals(listModel.get(i))) {
                        getWarningMassage("dd");
                        return;
                    }
                    arrId[i]=listModel.get(i).userId;

                }
                ArrayList<Integer> listEdges = mClient.getCommonFriends(user.userId,arrId);
                //mList.setListData(VKUser.arrayToStrings(mUsers));
                graph.getModel().beginUpdate();
                graph.setCellsMovable(true);
                try {
                    Object[] arr=graph.getChildVertices(graph.getDefaultParent());
                    Object vert1 = graph.insertVertex(graph.getDefaultParent(),((Integer)(user.userId)).toString(),user.firstName+" "+user.lastName,
                            110,100,50,50,
                            "verticalLabelPosition=bottom;fontColor=black;shape=image;image="+user.photoUrl);
                    //if(arr.length!=0)
                    //graph.insertEdge(graph.getDefaultParent(),null,"",vert1,arr[0]);
                    for(int i=0;i<listEdges.size();i++){
                        if(listEdges.get(i)==0)
                            continue;
                        for (Object c: arr) {
                            mxCell vert2=(mxCell)c;
                            if(Integer.parseInt(vert2.getId())==listModel.get(i).userId) {
                                graph.insertEdge(graph.getDefaultParent(),null,listEdges.get(i).toString(), vert1,vert2);
                            }
                        }
                    }
                }
                finally {
                    DeleteAll.setEnabled(true);
                    DelButton.setEnabled(true);
                    listModel.addElement(user);
                    layout.execute(graph.getDefaultParent());
                    graph.setCellsMovable(false);
                    graph.getModel().endUpdate();
                }
                count++;
                // TODO: add vertex and edges
            }
        });

        ImageIcon refrImg = new ImageIcon(System.getProperty("user.dir")+"/assets/Icons/kraskal.png");
        JButton KraskalButton = new JButton("",refrImg);
        KraskalButton.setBackground(colorForTools);
        KraskalButton.setSize(new Dimension(1000,1000));
        KraskalButton.setEnabled(false);

        buttonBar.add(AddButton);
        buttonBar.add(DelButton);
        buttonBar.add(DeleteAll);
        buttonBar.add(KraskalButton);
        buttonBar.setSize(Toolkit.getDefaultToolkit().getScreenSize().width,400);

        toolBar.add( buttonBar, BorderLayout.WEST);
        getContentPane().add( toolBar, BorderLayout.NORTH);
    }

    protected void makeLeftPane(mxGraphComponent graphComponent){
        LeftPanel = new JPanel();
        LeftPanel.setBackground(colorForTools);
        LeftPanel.setLayout(new BorderLayout());

        listModel=new DefaultListModel<>();
        mList = new JList(listModel);
        mList.setFixedCellWidth(leftPanelWidth);
        mList.setBackground(colorForTools);
        mxGraphOutline graphOutline = new mxGraphOutline(graphComponent);
        graphOutline.setPreferredSize(new Dimension(leftPanelWidth,leftPanelWidth));
        InputField = new JTextField();
        InputField.setColumns(colNum);
        InputField.setBackground(colorForOther);

        JPanel panelForList = new JPanel();
        panelForList.setLayout(new BoxLayout(panelForList,BoxLayout.Y_AXIS));
        panelForList.setBackground(colorForTools);
        panelForList.add(InputField);
        panelForList.add(mList);

        LeftPanel.add( graphOutline, BorderLayout.PAGE_END);
        LeftPanel.add(panelForList, BorderLayout.PAGE_START);
        getContentPane().add(LeftPanel,BorderLayout.WEST);
    }

    private void setUpVKClient()
    {
        mClient = new VKClient();
        mUsers = new ArrayList<VKUser>();
    }
    private void getWarningMassage(String error){
        JOptionPane.showMessageDialog(null,error);
    }

    private void delVertex(String id){
        graph.getModel().beginUpdate();
        graph.setCellsMovable(true);
        try {
            Object[] arr=graph.getChildVertices(graph.getDefaultParent());
            for (Object c: arr) {
                mxCell vertex=(mxCell)c;
                if(id.equals(vertex.getId())) {
                    graph.removeCells(graph.getEdges(vertex));
                    graph.getModel().remove(vertex);
                }
            }

        }
        finally {
            layout.execute(graph.getDefaultParent());
            delVtxFromList(id);
            graph.setCellsMovable(false);
            graph.getModel().endUpdate();
        }
    }

    public void delVtxFromList(String id)
    {
        for(int i=0; i<listModel.size(); i++)
            if(listModel.get(i).userId == Integer.parseInt(id))
                listModel.remove(i);
    }

    public MainWindow() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (Exception e){
            //throw e;
        }
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUpVKClient();
        mxGraphComponent graphComponent= makeGraph();
        graphComponent.getViewport().setOpaque(true);
        graphComponent.getViewport().setBackground(colorForOther);
        getContentPane().setBackground(colorForTools);
        getContentPane().add(graphComponent, BorderLayout.CENTER);
        makeLeftPane(graphComponent);
        makeToolbar();

        setVisible(true);
    }
}

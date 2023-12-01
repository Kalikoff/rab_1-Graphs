package example.kalmykov403.graph;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import example.kalmykov403.graph.model.GraphItem;
import example.kalmykov403.graph.model.LinkItem;
import example.kalmykov403.graph.model.NodeItem;
import example.kalmykov403.graph.databinding.ActivityMainBinding;
import example.kalmykov403.graph.databinding.AddNodeDialogBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding = null;

    ArrayAdapter<GraphItem> graphsAdapter;
    public static LocalDB database;

    private boolean editEnable = false;
    private boolean deleteEnable = false;
    private boolean copyEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = new LocalDB(this, "database2.db", null, 1);
        graphsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        binding.listGraphs.setAdapter(graphsAdapter);
        binding.listGraphs.setOnItemClickListener((parent, view, position, id) -> {
            if (!editEnable && !deleteEnable && !copyEnable) {
                Intent i = new Intent(getApplicationContext(), SecondActivity.class);
                i.putExtra("GRAPH_ID", graphsAdapter.getItem(position).id);
                startActivity(i);
            }
            else if (editEnable) {
                openEditGraphDialog(graphsAdapter.getItem(position).id);
                editEnable = false;
            }
            else if (copyEnable) {
                database.addGraph(graphsAdapter.getItem(position).name);
                refreshGraphs();
                GraphItem newGraph = graphsAdapter.getItem(graphsAdapter.getCount() - 1);
                ArrayList<NodeItem> oldNodes = database.getNodesInGraph(graphsAdapter.getItem(position).id);
                for(NodeItem node : oldNodes) {
                    database.addNode(newGraph.id, node.name, node.x, node.y);
                }
                ArrayList<NodeItem> newNodes = database.getNodesInGraph(newGraph.id);
                Map<Integer, NodeItem> nodeMap = new HashMap<>();
                for(int i = 0; i < oldNodes.size(); i++) {
                    nodeMap.put(oldNodes.get(i).id, newNodes.get(i));
                }
                ArrayList<LinkItem> oldLinks = database.getLinks(graphsAdapter.getItem(position).id);
                for(LinkItem link : oldLinks) {
                    database.addLink(newGraph.id, link.text, nodeMap.get(link.firstNodeId), nodeMap.get(link.secondNodeId));
                }
                copyEnable = false;
            }
            else {
                database.deleteGraph(graphsAdapter.getItem(position).id);
                deleteEnable = false;
                refreshGraphs();
            }
        });

        refreshGraphs();

        binding.buttonNew.setOnClickListener(v -> {
            openAddGraphDialog();
        });

        binding.buttonEdit.setOnClickListener(v -> {
            deleteEnable = false;
            editEnable = true;
            copyEnable = false;
        });

        binding.buttonDelete.setOnClickListener(v -> {
            deleteEnable = true;
            editEnable = false;
            copyEnable = false;
        });

        binding.buttonCopy.setOnClickListener(v -> {
            deleteEnable = false;
            editEnable = false;
            copyEnable = true;
        });
    }

    private void refreshGraphs() {
        graphsAdapter.clear();
        graphsAdapter.addAll(database.getGraphs());

        binding.buttonEdit.setEnabled(!graphsAdapter.isEmpty());
        binding.buttonDelete.setEnabled(!graphsAdapter.isEmpty());
        binding.buttonCopy.setEnabled(!graphsAdapter.isEmpty());
    }

    private void openAddGraphDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        View dialogView = getLayoutInflater().inflate(R.layout.add_node_dialog, null);
        AddNodeDialogBinding dialogBinding = AddNodeDialogBinding.bind(dialogView);

        dialogBinding.tvTitle.setText(R.string.add_graph);

        dialogBinding.btnAdd.setOnClickListener(view -> {
            if (!dialogBinding.etName.getText().toString().isEmpty()) {
                database.addGraph(dialogBinding.etName.getText().toString());
                refreshGraphs();
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.getWindow().setLayout(300, 300);
        dialog.show();
    }

    private void openEditGraphDialog(Integer graphId) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        View dialogView = getLayoutInflater().inflate(R.layout.add_node_dialog, null);
        AddNodeDialogBinding dialogBinding = AddNodeDialogBinding.bind(dialogView);

        dialogBinding.tvTitle.setText(R.string.edit_graph);

        dialogBinding.btnAdd.setOnClickListener(view -> {
            if (!dialogBinding.etName.getText().toString().isEmpty()) {
                database.updateGraphName(graphId, dialogBinding.etName.getText().toString());
                refreshGraphs();
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.getWindow().setLayout(300, 300);
        dialog.show();
    }
}
package example.kalmykov403.graph;

import static example.kalmykov403.graph.MainActivity.database;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import example.kalmykov403.graph.model.LinkItem;
import example.kalmykov403.graph.model.NodeItem;
import example.kalmykov403.graph.databinding.ActivitySecondBinding;
import example.kalmykov403.graph.databinding.AddNodeDialogBinding;

public class SecondActivity extends AppCompatActivity {
    private ActivitySecondBinding binding = null;
    private Integer graphId;
    private boolean isConnection = false;
    private boolean isDelete = false;
    private NodeItem firstForConnect = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        graphId = getIntent().getIntExtra("GRAPH_ID", 0);
        binding.workspace.addListeners(
                (newNode, newLinks) -> {
                    database.updateNodePosition(newNode.id, newNode.x, newNode.y);
                    for (LinkItem link: newLinks) {
                        database.updateLink(link);
                    }
                },
                node -> {
                    if (isConnection) {
                        if (firstForConnect == null) {
                            firstForConnect = node;
                        } else {
                            if(!binding.workspace.isLinkExists(firstForConnect, node)) {
                                openAddLinkDialog(firstForConnect, node);
                            }
                            isConnection = false;
                            firstForConnect = null;
                        }
                    }

                    if (isDelete) {
                        database.deleteNode(node.id);
                        ArrayList<LinkItem> links = database.getLinks(graphId);
                        for (LinkItem link : links) {
                            if(link.firstNodeId == node.id || link.secondNodeId == node.id) {
                                database.deleteLink(link.id);
                            }
                        }
                        refreshNodes();
                    }
                });

        refreshNodes();

        binding.buttonExit.setOnClickListener( v -> {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        });

        binding.buttonAdd.setOnClickListener(v -> {
            openAddNodeDialog();
        });

        binding.buttonDelete.setOnClickListener(v -> {
            isDelete = true;
            isConnection = false;
        });

        binding.buttonAddLink.setOnClickListener( v -> {
            isConnection = true;
            isDelete = false;
        });
    }

    private void refreshNodes() {
        binding.workspace.refreshPoints(database.getNodesInGraph(graphId), database.getLinks(graphId));
        binding.workspace.invalidate();
    }

    private void openAddNodeDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        View dialogView = getLayoutInflater().inflate(R.layout.add_node_dialog, null);
        AddNodeDialogBinding dialogBinding = AddNodeDialogBinding.bind(dialogView);

        dialogBinding.tvTitle.setText(R.string.add_node);

        dialogBinding.btnAdd.setOnClickListener(view -> {
            if (!dialogBinding.etName.getText().toString().isEmpty()) {
                String name = dialogBinding.etName.getText().toString();
                database.addNode(graphId, name, 300f, 300F);
                refreshNodes();
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }

    private void openAddLinkDialog(NodeItem firstNode, NodeItem secondNode) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        View dialogView = getLayoutInflater().inflate(R.layout.add_node_dialog, null);
        AddNodeDialogBinding dialogBinding = AddNodeDialogBinding.bind(dialogView);

        dialogBinding.tvTitle.setText(R.string.add_link);

        dialogBinding.btnAdd.setOnClickListener(view -> {
            if (!dialogBinding.etName.getText().toString().isEmpty()) {
                String name = dialogBinding.etName.getText().toString();
                database.addLink(graphId, name, firstNode, secondNode);
                refreshNodes();
                dialog.dismiss();
            }
        });

        dialog.setView(dialogView);
        dialog.show();
    }
}
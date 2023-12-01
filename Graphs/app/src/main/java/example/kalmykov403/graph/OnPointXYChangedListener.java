package example.kalmykov403.graph;

import java.util.List;

import example.kalmykov403.graph.model.LinkItem;
import example.kalmykov403.graph.model.NodeItem;

public interface OnPointXYChangedListener {
    void onXYChanged(NodeItem newNode, List<LinkItem> newLinks);
}
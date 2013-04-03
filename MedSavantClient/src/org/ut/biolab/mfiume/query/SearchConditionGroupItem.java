package org.ut.biolab.mfiume.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.mfiume.query.SearchConditionItem.SearchConditionListener;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;

/**
 *
 * @author mfiume
 */
public class SearchConditionGroupItem extends SearchConditionItem implements SearchConditionListener {

    private final List<SearchConditionItem> items;
    private static int groupNo;
    private final int thisGroupNo;

    public enum QueryRelation {

        AND, OR
    };

    public SearchConditionGroupItem(SearchConditionGroupItem parent) {
        this(QueryRelation.AND, parent);
    }

    public SearchConditionGroupItem(QueryRelation r, SearchConditionItem i, SearchConditionGroupItem parent) {
        super(null, r, parent);

        thisGroupNo = (++groupNo);

        items = new ArrayList<SearchConditionItem>();
        if (i != null) {
            items.add(i);
            i.setParent(this);
        }

    }

    @Override
    public String getName() {
        return "Group " + thisGroupNo;
    }

    public boolean isFirstItem(SearchConditionItem item) {
        return items.indexOf(item) == 0;
    }

    public SearchConditionGroupItem(QueryRelation r, SearchConditionGroupItem parent) {
        this(r, null, parent);
    }

    @Override
    public String toString() {
        String s = "";

        for (int i = 0; i < items.size(); i++) {
            SearchConditionItem item = items.get(i);
            if (item instanceof SearchConditionGroupItem) {
                s += "(" + item.toString() + ")";
            } else {
                s += item.toString();
            }
            if (i != items.size()) {
                s += " " + this.getRelation() + " ";
            }
        }

        return s;
    }

    public void removeItem(SearchConditionItem i) {

        System.out.println("Trying to remove " + i.getName() + " left = " + items.size());
        i.removeListener(this);
        items.remove(i);
        i.setParent(null);

        // the only child is a group
        if (items.size() == 1 && items.get(0) instanceof SearchConditionGroupItem) {

            SearchConditionGroupItem child = (SearchConditionGroupItem) items.get(0);
            for (SearchConditionItem c : child.getItems()) {
                c.removeListener(child);
                child.items.remove(i);
                c.setParent(null);

                c.addListener(this);
                c.setParent(this);
                items.add(c);
            }

            child.removeListener(this);
            this.items.remove(child);
            child.setParent(null);

        }

        // remove the group entirely, parent notifies of update
        if (items.isEmpty() && this.getParent() != null) {
            this.getParent().removeItem(this);

            // notify listeners of change
        } else {
            fireSearchConditionChangedEvent();
            fireSearchConditionItemRemovedEvent(i);
        }
    }

    public void addItem(SearchConditionItem i, int atIndex) {
        i.addListener(this);
        i.setParent(this);
        items.add(atIndex, i);
        fireSearchConditionChangedEvent();
        fireSearchConditionItemAddedEvent(i);
    }

    public void addItem(SearchConditionItem i) {
        addItem(i, items.size()); // add to the end
    }

    public void createGroupFromItem(SearchConditionItem i) {
        i.removeListener(this);
        int indexOfItem = items.indexOf(i);
        items.remove(i);
        SearchConditionGroupItem g = new SearchConditionGroupItem(QueryRelation.AND, i, this);
        addItem(g, indexOfItem);
    }

    public void moveItemToGroupAtIndex(SearchConditionItem i, SearchConditionGroupItem g, int newIndex) {
        i.removeListener(this);
        items.remove(i);
        i.setParent(null);
        g.addItem(i);
        g.moveItemToIndex(i, newIndex);
        fireSearchConditionChangedEvent();
    }

    public void moveItemToGroup(SearchConditionItem i, SearchConditionGroupItem g) {
        i.removeListener(this);
        items.remove(i);
        i.setParent(null);
        g.addItem(i);
        fireSearchConditionChangedEvent();
    }

    public void moveItemToIndex(SearchConditionItem i, int newIndex) {
        int currentIndex = items.indexOf(i);
        items.remove(i);
        items.add(newIndex, i);
        fireSearchConditionChangedEvent();
    }

    public void clearItems() {
        for (SearchConditionItem i : items) {
            i.removeListener(this);
        }
        this.items.removeAll(items);
        fireSearchConditionChangedEvent();
    }

    public List<SearchConditionItem> getItems() {
        return this.items;
    }

    @Override
    public void searchConditionsChanged(SearchConditionItem m) {
        this.fireSearchConditionChangedEvent();
    }

    @Override
    public void searchConditionItemRemoved(SearchConditionItem m) {
        this.fireSearchConditionItemRemovedEvent(m);
    }

    @Override
    public void searchConditionItemAdded(SearchConditionItem m) {
        this.fireSearchConditionItemAddedEvent(m);
    }
}

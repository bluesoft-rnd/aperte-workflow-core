package org.aperteworkflow.util.vaadin.ui.table;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractContainer;

import java.util.Collection;
import java.util.Collections;

public class PagedTableContainer extends AbstractContainer implements Container.Indexed, Container.Sortable {
    private final Container.Indexed container;
    private int pageLength = 25;
    private int startIndex = 0;

    public PagedTableContainer(Container.Indexed container) {
        this.container = container;
    }

    public Container.Indexed getContainer() {
        return container;
    }

    public int getPageLength() {
        return pageLength;
    }

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    @Override
    public int size() {
        int rowsLeft = container.size() - startIndex;
        if (rowsLeft > pageLength) {
            return pageLength;
        }
        else {
            return rowsLeft;
        }
    }

    public int getRealSize() {
        return container.size();
    }

    public Object getIdByIndex(int index) {
        return container.getIdByIndex(index + startIndex);
    }

    @Override
    public Item getItem(Object itemId) {
        return container.getItem(itemId);
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        return container.getContainerPropertyIds();
    }

    @Override
    public Collection<?> getItemIds() {
        return container.getItemIds();
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return container.getContainerProperty(itemId, propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return container.getType(propertyId);
    }

    @Override
    public boolean containsId(Object itemId) {
        return container.containsId(itemId);
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        return container.addItem(itemId);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        return container.addItem();
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return container.removeItem(itemId);
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Object nextItemId(Object itemId) {
        return container.nextItemId(itemId);
    }

    public Object prevItemId(Object itemId) {
        return container.prevItemId(itemId);
    }

    public Object firstItemId() {
        return container.firstItemId();
    }

    public Object lastItemId() {
        return container.lastItemId();
    }

    public boolean isFirstId(Object itemId) {
        return container.isFirstId(itemId);
    }

    public boolean isLastId(Object itemId) {
        return container.isLastId(itemId);
    }

    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        return container.addItemAfter(previousItemId);
    }

    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        return container.addItemAfter(previousItemId, newItemId);
    }

    public int indexOfId(Object itemId) {
        return container.indexOfId(itemId);
    }

    public Object addItemAt(int index) throws UnsupportedOperationException {
        return container.addItemAt(index);
    }

    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        return container.addItemAt(index, newItemId);
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        if (container instanceof Container.Sortable) {
            ((Container.Sortable) container).sort(propertyId, ascending);
        }
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        if (container instanceof Container.Sortable) {
            return ((Container.Sortable) container).getSortableContainerPropertyIds();
        }
        return Collections.EMPTY_LIST;
    }
}

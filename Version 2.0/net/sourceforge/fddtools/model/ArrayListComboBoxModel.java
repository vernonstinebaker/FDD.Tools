package net.sourceforge.fddtools.model;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;


public class ArrayListComboBoxModel
        extends AbstractListModel
        implements MutableComboBoxModel
{

    private Object selected;
    private ArrayList arrayList;

    public ArrayListComboBoxModel(ArrayList inList)
    {
        arrayList = inList;
    }

    @Override
    public Object getSelectedItem()
    {
        return selected;
    }

    @Override
    public void setSelectedItem(Object object)
    {
        selected = object;
    }

    @Override
    public int getSize()
    {
        return arrayList.size();
    }

    @Override
    public Object getElementAt(int index)
    {
        return arrayList.get(index);
    }

    @Override
    public void addElement(Object object)
    {
        arrayList.add(object);
    }

    @Override
    public void removeElement(Object object)
    {
        arrayList.remove(object);
    }

    @Override
    public void insertElementAt(Object object, int index)
    {
        arrayList.add(index, object);
    }

    @Override
    public void removeElementAt(int index)
    {
        arrayList.remove(index);
    }
}

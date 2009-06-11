/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.fddtools.model;

import java.util.ArrayList;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import com.nebulon.xml.fddi.Project;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author vds
 */
public class WorkPackageTableModel extends DefaultTableModel
{
    ArrayList<WorkPackage> workPackageList = null;

    public WorkPackageTableModel(Project project)
    {
        if(((FDDINode) project).getAny() != null)
        {
            for(Object o : ((FDDINode) project).getAny())
            {
                if(o instanceof WorkPackage)
                    workPackageList.add((WorkPackage) o);
            }
        }
    }

    @Override
    public int getRowCount()
    {
        return workPackageList.size();
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getColumnCount()
    {
        return 2;
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if(rowIndex >= workPackageList.size())
        {
            return null;
        }
        WorkPackage wp = workPackageList.get(rowIndex);
        if(columnIndex == 0)
        {
            return wp.getSequence();
        }
        else if(columnIndex == 1)
        {
            return wp.getName();
        }
        else
        {
            return null;
    	}
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    List<WorkPackage> getWorkPackageList()
    {
        return workPackageList;
    }
}

package org.ow2.clif.jenkins.model;

import java.util.ArrayList;
import java.util.List;

public class ClifReport
{

    private List<TestPlan> testplans;

    public ClifReport()
    {
        testplans = new ArrayList<TestPlan>();
    }

    public List<TestPlan> getTestplans()
    {
        return testplans;
    }

    public TestPlan getTestplan( String testPlanName )
    {
        for ( TestPlan testPlan : testplans )
        {
            if (testPlanName.equals( testPlan.getName() ))
            {
                return testPlan;
            }

        }
        return null;
    }

    public void setTestplans( List<TestPlan> testplans )
    {
        this.testplans = testplans;
    }

    public void addTestplan( TestPlan testplan )
    {
        if (testplans == null)
        {
            testplans = new ArrayList<TestPlan>();
        }
        testplans.add( testplan );
    }


}

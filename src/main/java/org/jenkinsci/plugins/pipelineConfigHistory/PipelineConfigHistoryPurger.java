package org.jenkinsci.plugins.pipelineConfigHistory;

import hudson.model.PeriodicWork;

public class PipelineConfigHistoryPurger extends PeriodicWork {

  @Override
  public long getRecurrencePeriod() {
    //purge on a daily basis
    return DAY;
  }

  @Override
  protected void doRun() throws Exception {

  }
}

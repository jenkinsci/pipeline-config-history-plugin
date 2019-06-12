package org.jenkinsci.plugins.pipelineConfigHistory;

import hudson.Extension;
import hudson.model.PeriodicWork;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipelineConfigHistory.model.PipelineConfigHistoryGlobalConfiguration;
import org.jenkinsci.plugins.pipelineConfigHistory.model.PipelineItemHistoryDao;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class PipelineConfigHistoryPurger extends PeriodicWork {


  /**
   * This classes loger.
   */
  private static final Logger LOG =
      Logger.getLogger(PipelineConfigHistoryPurger.class.getName());

  @Override
  public long getRecurrencePeriod() {
    //purge on a daily basis
    return DAY;
  }

  @Override
  protected void doRun() throws Exception {
    //stateless, maxAge always comes from the global config.
    Optional<Integer> maxAgeOptional = PipelineConfigHistoryGlobalConfiguration.get().getMaxDaysToKeepEntriesOptional();
    if (!maxAgeOptional.isPresent()) return;

    int maxAge = maxAgeOptional.get();
    if (maxAge > 0) {
      LOG.log(Level.FINE,
          "checking for history files to purge (max age of {0} days allowed)",
          maxAge);
      purgeHistoryByAge(maxAge);
    }
  }

  private void purgeHistoryByAge(int maxAge) {
    PipelineItemHistoryDao pipelineItemHistoryDao = PluginUtils.getHistoryDao();
    Jenkins.get().getAllItems(WorkflowJob.class).forEach( workflowJob ->
        pipelineItemHistoryDao.purgeEntriesByAge(maxAge, workflowJob)
    );
  }
}

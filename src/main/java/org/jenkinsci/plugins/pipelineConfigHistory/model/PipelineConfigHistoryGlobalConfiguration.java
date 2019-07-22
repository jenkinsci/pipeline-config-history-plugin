package org.jenkinsci.plugins.pipelineConfigHistory.model;


import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Optional;
import javax.swing.text.html.Option;

@Extension
@SuppressWarnings("unused")
public class PipelineConfigHistoryGlobalConfiguration extends GlobalConfiguration {

  /** Maximum number of configuration history entries to keep. */
  private String maxHistoryEntries;

  /** Maximum number of days to keep entries. */
  private String maxDaysToKeepEntries;


  public PipelineConfigHistoryGlobalConfiguration() {
    load();
  }

  public static PipelineConfigHistoryGlobalConfiguration get() {
    return GlobalConfiguration.all().get(PipelineConfigHistoryGlobalConfiguration.class);
  }

  @Override
  public boolean configure(StaplerRequest request, JSONObject formData) {

    maxHistoryEntries = formData.getString("maxHistoryEntries").trim();
    maxDaysToKeepEntries = formData.getString("maxDaysToKeepEntries").trim();

    save();
    return true;
  }

  public String getMaxHistoryEntries() { return maxHistoryEntries;}

  public String getMaxDaysToKeepEntries() { return  maxDaysToKeepEntries;}


  public Optional<Integer> getMaxHistoryEntriesOptional() {
    try {
      return Optional.of(Integer.parseInt(maxHistoryEntries));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  public Optional<Integer> getMaxDaysToKeepEntriesOptional() {
    try {
      return Optional.of(Integer.parseInt(maxDaysToKeepEntries));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  /**
   * Validates the user entry for the maximum number of history items to keep.
   * Must be blank or a non-negative integer.
   *
   * @param value
   *            The form input entered by the user.
   * @return ok if the entry is blank or a non-negative integer.
   */
  public FormValidation doCheckMaxHistoryEntries(
      @QueryParameter final String value) {
    try {
      if (StringUtils.isNotBlank(value) && Integer.parseInt(value) < 0) {
        throw new NumberFormatException();
      }
      return FormValidation.ok();
    } catch (NumberFormatException ex) {
      return FormValidation.error("Enter a valid positive integer");
    }
  }

  /**
   * Validates the user entry for the maximum number of days to keep history
   * items. Must be blank or a non-negative integer.
   *
   * @param value
   *            The form input entered by the user.
   * @return ok if the entry is blank or a non-negative integer.
   */
  public FormValidation doCheckMaxDaysToKeepEntries(
      @QueryParameter final String value) {
    try {
      if (StringUtils.isNotBlank(value) && Integer.parseInt(value) < 0) {
        throw new NumberFormatException();
      }
      return FormValidation.ok();
    } catch (NumberFormatException ex) {
      return FormValidation.error("Enter a valid positive integer");
    }
  }
}

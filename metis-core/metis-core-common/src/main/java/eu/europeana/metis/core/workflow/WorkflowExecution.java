package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.json.ObjectIdSerializer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;

/**
 * Is the structure where the combined plugins of harvesting and the other plugins will be stored.
 * <p>This is the object where the execution of the workflow takes place and will host all information,
 * regarding its execution.</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
@Entity
@Indexes({@Index(fields = {@Field("workflowOwner"), @Field("workflowName")})})
public class WorkflowExecution implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private int datasetId;
  @Indexed
  private String workflowOwner;
  @Indexed
  private String workflowName;
  @Indexed
  private WorkflowStatus workflowStatus;
  @Indexed
  private String ecloudDatasetId;
  private int workflowPriority;
  private boolean cancelling;

  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date createdDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date startedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date updatedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date finishedDate;

  private List<AbstractMetisPlugin> metisPlugins = new ArrayList<>();

  public WorkflowExecution() {
    //Required for json serialization
  }

  /**
   * Constructor with all required parameters and initializes it's internal structure.
   *
   * @param dataset the {@link Dataset} related to the execution
   * @param workflow the {@link Workflow} related to the execution
   * @param metisPlugins the list of {@link AbstractMetisPlugin} including harvest plugin for execution
   * @param workflowPriority the positive number of the priority of the execution
   */
  public WorkflowExecution(Dataset dataset, Workflow workflow,
      List<AbstractMetisPlugin> metisPlugins,
      int workflowPriority) {
    this.workflowOwner = workflow.getWorkflowOwner();
    this.workflowName = workflow.getWorkflowName();
    this.datasetId = dataset.getDatasetId();
    this.ecloudDatasetId = dataset.getEcloudDatasetId();
    this.workflowPriority = workflowPriority;
    this.metisPlugins = metisPlugins;
  }

  /**
   * Sets all plugins inside the execution, that have status {@link PluginStatus#INQUEUE} or
   * {@link PluginStatus#RUNNING}, to {@link PluginStatus#CANCELLED}
   */
  public void setAllRunningAndInqueuePluginsToCancelled() {
    this.setWorkflowStatus(WorkflowStatus.CANCELLED);
    for (AbstractMetisPlugin metisPlugin :
        this.getMetisPlugins()) {
      if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
          || metisPlugin.getPluginStatus() == PluginStatus.RUNNING) {
        metisPlugin.setPluginStatus(PluginStatus.CANCELLED);
      }
    }
    this.setCancelling(false);
  }

  /**
   * Checks if one of the plugins has {@link PluginStatus#FAILED} and if yes sets all other plugins
   * that have status {@link PluginStatus#INQUEUE} or {@link PluginStatus#RUNNING}, to {@link PluginStatus#CANCELLED}
   */
  public void checkAndSetAllRunningAndInqueuePluginsToFailedIfOnePluginHasFailed() {
    boolean hasAPluginFailed = false;
    for (AbstractMetisPlugin metisPlugin : this.getMetisPlugins()) {
      if (metisPlugin.getPluginStatus() == PluginStatus.FAILED) {
        hasAPluginFailed = true;
        break;
      }
    }
    if (hasAPluginFailed) {
      this.setWorkflowStatus(WorkflowStatus.FAILED);
      for (AbstractMetisPlugin metisPlugin : this.getMetisPlugins()) {
        if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
            || metisPlugin.getPluginStatus() == PluginStatus.RUNNING) {
          metisPlugin.setPluginStatus(PluginStatus.CANCELLED);
        }
      }
    }
  }

  @Override
  public ObjectId getId() {
    return id;
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  public boolean isCancelling() {
    return cancelling;
  }

  public void setCancelling(boolean cancelling) {
    this.cancelling = cancelling;
  }

  public String getWorkflowOwner() {
    return workflowOwner;
  }

  public void setWorkflowOwner(String workflowOwner) {
    this.workflowOwner = workflowOwner;
  }

  public String getWorkflowName() {
    return workflowName;
  }

  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }

  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  public int getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(int datasetId) {
    this.datasetId = datasetId;
  }

  public String getEcloudDatasetId() {
    return ecloudDatasetId;
  }

  public void setEcloudDatasetId(String ecloudDatasetId) {
    this.ecloudDatasetId = ecloudDatasetId;
  }

  public int getWorkflowPriority() {
    return workflowPriority;
  }

  public void setWorkflowPriority(int workflowPriority) {
    this.workflowPriority = workflowPriority;
  }

  public Date getCreatedDate() {
    return createdDate == null ? null : new Date(createdDate.getTime());
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate == null ? null : new Date(createdDate.getTime());
  }

  public Date getStartedDate() {
    return startedDate == null ? null : new Date(startedDate.getTime());
  }

  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate == null ? null : new Date(startedDate.getTime());
  }

  public Date getFinishedDate() {
    return finishedDate == null ? null : new Date(finishedDate.getTime());
  }

  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate == null ? null : new Date(finishedDate.getTime());
  }

  public Date getUpdatedDate() {
    return updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public List<AbstractMetisPlugin> getMetisPlugins() {
    return metisPlugins;
  }

  public void setMetisPlugins(
      List<AbstractMetisPlugin> metisPlugins) {
    this.metisPlugins = metisPlugins;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + Integer.hashCode(datasetId);
    result = prime * result + ((workflowOwner == null) ? 0 : workflowOwner.hashCode());
    return prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    WorkflowExecution that = (WorkflowExecution) obj;
    return (id == that.getId() && datasetId == that.datasetId && workflowOwner
        .equals(that.workflowOwner)
        && workflowName.equals(that.workflowName));
  }
}



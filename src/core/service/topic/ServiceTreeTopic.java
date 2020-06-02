package core.service.topic;

import com.intellij.util.messages.Topic;
import core.beans.Request;

import java.util.List;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface ServiceTreeTopic extends RestTopic<Map<String, List<Request>>> {

    Topic<ServiceTreeTopic> ACTION_SCAN_SERVICE = Topic.create("RestTopic.ServiceTreeTopic", ServiceTreeTopic.class);

    /**
     * action
     *
     * @param data data
     */
    @Override
    void action(Map<String, List<Request>> data);
}

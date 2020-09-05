package com.github.restful.tool.service.topic;

import com.intellij.util.messages.Topic;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface RefreshServiceTreeTopic extends RestTopic<Void> {

    Topic<RefreshServiceTreeTopic> TOPIC = Topic.create("RestTopic.Refresh-ServiceTreeTopic", RefreshServiceTreeTopic.class);

    /**
     * refresh
     */
    void refresh();
}

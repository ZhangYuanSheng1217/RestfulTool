package core.service.topic;

import com.intellij.util.messages.Topic;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface RefreshServiceTreeTopic extends RestTopic<Void> {

    Topic<RefreshServiceTreeTopic> TOPIC = Topic.create("RestTopic.Refresh-ServiceTreeTopic", RefreshServiceTreeTopic.class);

    /**
     * action
     *
     * @param data data
     * @see RefreshServiceTreeTopic#refresh()
     */
    @Deprecated
    @Override
    default void action(Void data) {
    }

    /**
     * refresh
     */
    void refresh();
}

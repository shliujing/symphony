/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.service;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Emotion;
import org.b3log.symphony.repository.EmotionRepository;
import org.json.JSONObject;

/**
 * Emotion management service.
 *
 * @author Zephyr
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Aug 18, 2016
 * @since 1.5.0
 */
@Service
public class EmotionMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(EmotionMgmtService.class);

    /**
     * Emotion repository.
     */
    @Inject
    private EmotionRepository emotionRepository;

    /**
     * Sets a user's emotions.
     *
     * @param userId the specified user id
     * @param emotionList the specified emotions
     * @throws ServiceException service exception
     */
    public void setEmotionList(final String userId, final String emotionList) throws ServiceException {
        final Transaction transaction = emotionRepository.beginTransaction();

        try {
            // clears the user all emotions
            emotionRepository.removeUserEmotions(userId);

            final Set<String> emotionSet = new HashSet<>(); // for deduplication
            final String[] emotionArray = emotionList.split(",");
            for (int i = 0, sort = 0; i < emotionArray.length; i++) {
                final String content = emotionArray[i];
                if (StringUtils.isBlank(content) || emotionSet.contains(content)) {
                    continue;
                }

                final JSONObject userEmotion = new JSONObject();
                userEmotion.put(Emotion.EMOTION_USER_ID, userId);
                userEmotion.put(Emotion.EMOTION_CONTENT, content);
                userEmotion.put(Emotion.EMOTION_SORT, sort++);
                userEmotion.put(Emotion.EMOTION_TYPE, Emotion.EMOTION_TYPE_C_EMOJI);

                emotionRepository.add(userEmotion);

                emotionSet.add(content);
            }

            transaction.commit();
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Set user emotion list failed [id=" + userId + "]", e);
            if (null != transaction && transaction.isActive()) {
                transaction.rollback();
            }

            throw new ServiceException(e);
        }
    }
}

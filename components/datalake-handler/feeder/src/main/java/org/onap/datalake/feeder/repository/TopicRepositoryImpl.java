/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
* ================================================================================
* Copyright 2019 China Mobile
*=================================================================================
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
* ============LICENSE_END=========================================================
*/
package org.onap.datalake.feeder.repository;

import org.onap.datalake.feeder.domain.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
/*
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;  

import com.mongodb.WriteResult;
import com.mongodb.client.result.UpdateResult;
*/
import java.util.List;

/**
 * @author Guobiao Mo
 *
 */
public class TopicRepositoryImpl implements TopicRepositoryCustom {

    @Autowired
    CouchbaseTemplate template;
    
    @Override
    public long updateTopic(String topic, Boolean state) {
/*
        Query query = new Query(Criteria.where("id").is(topic));
        Update update = new Update();
        update.set("state", state);

        UpdateResult result = mongoTemplate.updateFirst(query, update, Topic.class);

        if(result!=null)
            return result.getModifiedCount();
        else
  */          return 0L;
    	
    	
    	
    }
}

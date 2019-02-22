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
import org.springframework.data.couchbase.core.query.N1qlPrimaryIndexed;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.core.query.ViewIndexed;
import org.springframework.data.couchbase.repository.CouchbasePagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 

import java.util.List;

/**
 * 
 * Topic Repository interface, implementation is taken care by Spring framework.
 * Customization is done through TopicRepositoryCustom and its implementation TopicRepositoryImpl. 
 * 
 * @author Guobiao Mo
 *
 */
@ViewIndexed(designDoc = "topic", viewName = "all")
public interface TopicRepository extends CouchbasePagingAndSortingRepository<Topic, String>, TopicRepositoryCustom {
/*
	Topic findFirstById(String topic);

	Topic findByIdAndState(String topic, boolean state);

    //Supports native JSON query string
    @Query("{topic:'?0'}")
    Topic findTopicById(String topic);

    @Query("{topic: { $regex: ?0 } })")
    List<Topic> findTopicByRegExId(String topic);


    //Page<Topic> findByCompanyIdAndNameLikeOrderByName(String companyId, String name, Pageable pageable);

    @Query("#{#n1ql.selectEntity} where #{#n1ql.filter} and companyId = $1 and $2 within #{#n1ql.bucket}")
    Topic findByCompanyAndAreaId(String companyId, String areaId);

    @Query("#{#n1ql.selectEntity} where #{#n1ql.filter} AND ANY phone IN phoneNumbers SATISFIES phone = $1 END")
    List<Topic> findByPhoneNumber(String telephoneNumber);

    @Query("SELECT COUNT(*) AS count FROM #{#n1ql.bucket} WHERE #{#n1ql.filter} and companyId = $1")
    Long countBuildings(String companyId);
    */
}

package com.sabujaks.irs.domain.data_init.repository;

import com.sabujaks.irs.domain.data_init.model.entity.BaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.PatternSyntaxException;

@Repository
public interface BaseInfoRepository extends JpaRepository<BaseInfo, Long> {
    List<BaseInfo> findAllByGroupName(String groupName);

    @Query("SELECT b FROM BaseInfo b WHERE b.groupName = :groupName AND b.code IN :codes")
    List<BaseInfo> findAllByGroupNameAndCodeIn(@Param("groupName") String groupName, @Param("codes") List<String> codes);

    List<BaseInfo> findByCodeIn(List<String> companyBenefitsCodes);

    @Query("SELECT b FROM BaseInfo b WHERE b.code LIKE %:inCode% AND b.parentCode IS NULL")
    List<BaseInfo> findAllByCodeContainingAndParentCodeIsNull(@Param("inCode") String inCode);

    @Query("SELECT b FROM BaseInfo b where b.parentCode = :code")
    List<BaseInfo> findAllByParentCode(@Param("code") String code);

    // 코드로 엔티티 찾기
    BaseInfo findByCode(String code);

}
package org.changppo.account.repository.apikey;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.changppo.account.service.dto.apikey.ApiKeyDto;
import org.changppo.account.service.dto.apikey.QApiKeyDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.changppo.account.entity.apikey.QApiKey.apiKey;
import static org.changppo.account.type.GradeType.GRADE_FREE;

@RequiredArgsConstructor
public class ApiKeyRepositoryImpl implements QuerydslApiKeyRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ApiKeyDto> findAllByMemberIdOrderByAsc(Long memberId, Long firstApiKeyId, Pageable pageable) {
        List<ApiKeyDto> apiKeyDtos = queryFactory.select(
                new QApiKeyDto(
                        apiKey.id,
                        apiKey.value,
                        apiKey.grade.gradeType,
                        apiKey.paymentFailureBannedAt,
                        apiKey.cardDeletionBannedAt,
                        apiKey.createdAt))
                .from(apiKey)
                .where(memberIdEquals(memberId).and(apiKeyIdGreaterThanOrEqual(firstApiKeyId)))
                .orderBy(apiKey.id.asc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = apiKeyDtos.size() > pageable.getPageSize();
        if (hasNext) {
            apiKeyDtos.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(apiKeyDtos, pageable, hasNext);
    }


    @Override
    public void deleteAllByMemberId(Long memberId) {
        queryFactory.delete(apiKey)
                .where(memberIdEquals(memberId))
                .execute();
    }

    @Override
    public void banForCardDeletionByMemberId(Long memberId, LocalDateTime time) {
        queryFactory.update(apiKey)
                .set(apiKey.cardDeletionBannedAt, time)
                .where(memberIdEquals(memberId).and(statusNotGradeFree()))
                .execute();
    }

    @Override
    public void unbanForCardDeletionByMemberId(Long memberId) {
        queryFactory.update(apiKey)
                .set(apiKey.cardDeletionBannedAt, (LocalDateTime) null)
                .where(memberIdEquals(memberId).and(statusNotGradeFree()))
                .execute();
    }

    @Override
    public void banApiKeysForPaymentFailure(Long memberId, LocalDateTime time) {
        queryFactory.update(apiKey)
                .set(apiKey.paymentFailureBannedAt, time)
                .where(memberIdEquals(memberId))
                .execute();
    }

    @Override
    public void unbanApiKeysForPaymentFailure(Long memberId) {
        queryFactory.update(apiKey)
                .set(apiKey.paymentFailureBannedAt, (LocalDateTime) null)
                .where(memberIdEquals(memberId))
                .execute();
    }

    @Override
    public void requestApiKeyDeletion(Long memberId, LocalDateTime time) {
        queryFactory.update(apiKey)
                .set(apiKey.deletionRequestedAt, time)
                .where(memberIdEquals(memberId))
                .execute();
    }

    @Override
    public void cancelApiKeyDeletionRequest(Long memberId) {
        queryFactory.update(apiKey)
                .set(apiKey.deletionRequestedAt, (LocalDateTime) null)
                .where(memberIdEquals(memberId))
                .execute();
    }
    private BooleanExpression memberIdEquals(Long memberId) {
        return apiKey.member.id.eq(memberId);
    }

    private BooleanExpression apiKeyIdGreaterThanOrEqual(Long apiKeyId) {
        return apiKeyId != null ? apiKey.id.goe(apiKeyId) : null;
    }

    private BooleanExpression statusNotGradeFree() {
        return apiKey.grade.gradeType.ne(GRADE_FREE);
    }
}
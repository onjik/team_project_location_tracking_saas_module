package org.changppo.cost_management_service.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.changppo.cost_management_service.entity.member.RoleType;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String name;
    private String username;
    private String profileImage;
    private Set<RoleType> roles;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime bannedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;
}
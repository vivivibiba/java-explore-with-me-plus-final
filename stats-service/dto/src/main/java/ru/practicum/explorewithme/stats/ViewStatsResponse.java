package ru.practicum.explorewithme.stats;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class ViewStatsResponse {
    private String app;
    private String uri;
    private long hits;
}

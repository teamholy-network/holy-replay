package de.teamholy.replay.database.entity;

import eu.koboo.en2do.repository.entity.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReplayEntity {

    @Id
    private String id;
    private int duration;
    private long time;
    private byte[] data;
}


package de.teamholy.replay.database.repository;

import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import de.teamholy.replay.database.entity.ReplayEntity;
import lombok.NoArgsConstructor;

@Collection("replay_collection")
public interface ReplayRepository extends Repository<ReplayEntity, String>, AsyncRepository<ReplayEntity, String> {
}


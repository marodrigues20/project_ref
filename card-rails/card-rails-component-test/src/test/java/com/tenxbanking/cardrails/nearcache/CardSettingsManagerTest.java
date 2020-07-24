package com.tenxbanking.cardrails.nearcache;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_SETTINGS;
import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static com.tenxbanking.cardrails.stub.DebitCardManagerWiremockStubs.stubGetCardSettings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_SECOND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.cards.CardSettingsManager;
import com.tenxbanking.cardrails.adapter.secondary.redis.CardSettingsRedisRepository;
import com.tenxbanking.cardrails.data.CardSettingsDataFactory;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CardSettingsManagerTest extends BaseComponentTest {

  @Autowired
  private CardSettingsRedisRepository cardSettingsRedisRepository;

  @Autowired
  private CardSettingsManager underTest;

  @Before
  public void before() {
    cardSettingsRedisRepository.deleteAll();
  }

  @Test
  public void getsFromRedisIfPresent() {

    cardSettingsRedisRepository.save(CARD_SETTINGS);

    Optional<CardSettings> actual = underTest.findByCardIdOrPanHash(CARD_ID, PAN_HASH);

    assertThat(actual).contains(CARD_SETTINGS);
  }

  @Test
  public void whenNotPresentInRedis_thenGetsFromRestCallAndSavesInRedis() throws JsonProcessingException {

    stubGetCardSettings(CardSettingsDataFactory.getCardSettingsResponse());

    Optional<CardSettings> actual = underTest.findByCardIdOrPanHash(CARD_ID, PAN_HASH);
    assertThat(actual).contains(CARD_SETTINGS);

    await().atMost(ONE_SECOND).untilAsserted(() -> {
      Optional<CardSettings> cachedSettings = cardSettingsRedisRepository.findById(PAN_HASH);
      assertThat(cachedSettings).contains(CARD_SETTINGS);
    });
  }

}

package br.gov.crateus.bcm.sicim.domain;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import java.math.BigDecimal;

/** Ponto WGS84 (SRID 4326) restrito ao bounding box do município de Crateús/CE. */
public record Geolocation(BigDecimal latitude, BigDecimal longitude) {

	static final BigDecimal LAT_MIN = new BigDecimal("-5.65");
	static final BigDecimal LAT_MAX = new BigDecimal("-4.70");
	static final BigDecimal LNG_MIN = new BigDecimal("-41.20");
	static final BigDecimal LNG_MAX = new BigDecimal("-40.10");

	public Geolocation {
		if (latitude == null || longitude == null
				|| latitude.compareTo(LAT_MIN) < 0 || latitude.compareTo(LAT_MAX) > 0
				|| longitude.compareTo(LNG_MIN) < 0 || longitude.compareTo(LNG_MAX) > 0) {
			throw SicimDomainException.validation(
					"Geolocation (" + latitude + ", " + longitude + ") is outside the municipality of Crateús/CE.");
		}
	}
}

package br.gov.crateus.bcm.sicim.infrastructure.persistence;

import br.gov.crateus.bcm.sicim.domain.PossessionType;
import br.gov.crateus.bcm.sicim.domain.PropertyStatus;
import br.gov.crateus.bcm.sicim.domain.UsageCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties", schema = "sicim")
public class PropertyEntity extends SicimAuditableEntity {

	@Column(name = "registration_number", length = 20, nullable = false, unique = true, updatable = false)
	private String registrationNumber;

	@Column(name = "notary_office", nullable = false)
	private String notaryOffice;

	@Column(name = "notarial_description", nullable = false, columnDefinition = "text")
	private String notarialDescription;

	@Column(name = "address_street", nullable = false)
	private String addressStreet;

	@Column(name = "address_number", length = 20, nullable = false)
	private String addressNumber;

	@Column(name = "address_neighborhood", length = 100, nullable = false)
	private String addressNeighborhood;

	@Column(name = "neighborhood_id")
	private UUID neighborhoodId;

	@Column(name = "address_zip_code", length = 9, nullable = false)
	private String addressZipCode;

	@Column(name = "address_reference")
	private String addressReference;

	@Column(name = "total_area", precision = 12, scale = 2, nullable = false)
	private BigDecimal totalArea;

	@Column(name = "built_area", precision = 12, scale = 2, nullable = false)
	private BigDecimal builtArea;

	@Column(name = "latitude", precision = 9, scale = 6, nullable = false)
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 9, scale = 6, nullable = false)
	private BigDecimal longitude;

	@Column(name = "managing_unit_id", nullable = false)
	private UUID managingUnitId;

	@Column(name = "budget_unit", length = 100)
	private String budgetUnit;

	@Enumerated(EnumType.STRING)
	@Column(name = "usage_category", length = 32, nullable = false)
	private UsageCategory usageCategory;

	@Column(name = "custom_category_name", length = 100)
	private String customCategoryName;

	@Enumerated(EnumType.STRING)
	@Column(name = "possession_type", length = 32, nullable = false)
	private PossessionType possessionType;

	@Column(name = "contract_start_date")
	private OffsetDateTime contractStartDate;

	@Column(name = "contract_end_date")
	private OffsetDateTime contractEndDate;

	@Column(name = "contract_monthly_value", precision = 12, scale = 2)
	private BigDecimal contractMonthlyValue;

	@Column(name = "contract_reference_value", precision = 12, scale = 2)
	private BigDecimal contractReferenceValue;

	@Column(name = "contract_grantor")
	private String contractGrantor;

	@Column(name = "contract_lessor")
	private String contractLessor;

	@Column(name = "contract_administrative_process_number", length = 100)
	private String contractAdministrativeProcessNumber;

	@Column(name = "acquisition_year", nullable = false)
	private Integer acquisitionYear;

	@Column(name = "original_value", precision = 14, scale = 2, nullable = false)
	private BigDecimal originalValue;

	@Column(name = "accumulated_depreciation", precision = 14, scale = 2, nullable = false)
	private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

	@Column(name = "public_purpose", nullable = false, columnDefinition = "text")
	private String publicPurpose;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 32, nullable = false)
	private PropertyStatus status;

	@Column(name = "approved_by")
	private String approvedBy;

	@Column(name = "approved_at")
	private OffsetDateTime approvedAt;

	public String getRegistrationNumber() { return registrationNumber; }
	public void setRegistrationNumber(String v) { this.registrationNumber = v; }
	public String getNotaryOffice() { return notaryOffice; }
	public void setNotaryOffice(String v) { this.notaryOffice = v; }
	public String getNotarialDescription() { return notarialDescription; }
	public void setNotarialDescription(String v) { this.notarialDescription = v; }
	public String getAddressStreet() { return addressStreet; }
	public void setAddressStreet(String v) { this.addressStreet = v; }
	public String getAddressNumber() { return addressNumber; }
	public void setAddressNumber(String v) { this.addressNumber = v; }
	public String getAddressNeighborhood() { return addressNeighborhood; }
	public void setAddressNeighborhood(String v) { this.addressNeighborhood = v; }
	public UUID getNeighborhoodId() { return neighborhoodId; }
	public void setNeighborhoodId(UUID v) { this.neighborhoodId = v; }
	public String getAddressZipCode() { return addressZipCode; }
	public void setAddressZipCode(String v) { this.addressZipCode = v; }
	public String getAddressReference() { return addressReference; }
	public void setAddressReference(String v) { this.addressReference = v; }
	public BigDecimal getTotalArea() { return totalArea; }
	public void setTotalArea(BigDecimal v) { this.totalArea = v; }
	public BigDecimal getBuiltArea() { return builtArea; }
	public void setBuiltArea(BigDecimal v) { this.builtArea = v; }
	public BigDecimal getLatitude() { return latitude; }
	public void setLatitude(BigDecimal v) { this.latitude = v; }
	public BigDecimal getLongitude() { return longitude; }
	public void setLongitude(BigDecimal v) { this.longitude = v; }
	public UUID getManagingUnitId() { return managingUnitId; }
	public void setManagingUnitId(UUID v) { this.managingUnitId = v; }
	public String getBudgetUnit() { return budgetUnit; }
	public void setBudgetUnit(String v) { this.budgetUnit = v; }
	public UsageCategory getUsageCategory() { return usageCategory; }
	public void setUsageCategory(UsageCategory v) { this.usageCategory = v; }
	public String getCustomCategoryName() { return customCategoryName; }
	public void setCustomCategoryName(String v) { this.customCategoryName = v; }
	public PossessionType getPossessionType() { return possessionType; }
	public void setPossessionType(PossessionType v) { this.possessionType = v; }
	public OffsetDateTime getContractStartDate() { return contractStartDate; }
	public void setContractStartDate(OffsetDateTime v) { this.contractStartDate = v; }
	public OffsetDateTime getContractEndDate() { return contractEndDate; }
	public void setContractEndDate(OffsetDateTime v) { this.contractEndDate = v; }
	public BigDecimal getContractMonthlyValue() { return contractMonthlyValue; }
	public void setContractMonthlyValue(BigDecimal v) { this.contractMonthlyValue = v; }
	public BigDecimal getContractReferenceValue() { return contractReferenceValue; }
	public void setContractReferenceValue(BigDecimal v) { this.contractReferenceValue = v; }
	public String getContractGrantor() { return contractGrantor; }
	public void setContractGrantor(String v) { this.contractGrantor = v; }
	public String getContractLessor() { return contractLessor; }
	public void setContractLessor(String v) { this.contractLessor = v; }
	public String getContractAdministrativeProcessNumber() { return contractAdministrativeProcessNumber; }
	public void setContractAdministrativeProcessNumber(String v) { this.contractAdministrativeProcessNumber = v; }
	public Integer getAcquisitionYear() { return acquisitionYear; }
	public void setAcquisitionYear(Integer v) { this.acquisitionYear = v; }
	public BigDecimal getOriginalValue() { return originalValue; }
	public void setOriginalValue(BigDecimal v) { this.originalValue = v; }
	public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
	public void setAccumulatedDepreciation(BigDecimal v) { this.accumulatedDepreciation = v; }
	public String getPublicPurpose() { return publicPurpose; }
	public void setPublicPurpose(String v) { this.publicPurpose = v; }
	public PropertyStatus getStatus() { return status; }
	public void setStatus(PropertyStatus v) { this.status = v; }
	public String getApprovedBy() { return approvedBy; }
	public void setApprovedBy(String v) { this.approvedBy = v; }
	public OffsetDateTime getApprovedAt() { return approvedAt; }
	public void setApprovedAt(OffsetDateTime v) { this.approvedAt = v; }
}

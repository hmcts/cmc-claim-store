package uk.gov.hmcts.cmc.claimstore.services.user;

public interface IUserAuthorisationTokenService {
    String getAuthorisationToken(String username, String password);
}

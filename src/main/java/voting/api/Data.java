package voting.api;

// A Bundle class to group all of polling data
// Makes it convenient to access DataBase with this Bundle kind of class
public final class Data {

    public String title, poll_date, candidates, voters, numCandidates, numVoters;

    public Data(String title, String poll_date, String candidates, String voters, String numCandidates,
                    String numVoters) throws Exception {
        this.title = title;
        this.poll_date = poll_date;
        this.candidates = candidates;
        this.voters = voters;
        this.numCandidates = numCandidates;
        this.numVoters = numVoters;
    }

}

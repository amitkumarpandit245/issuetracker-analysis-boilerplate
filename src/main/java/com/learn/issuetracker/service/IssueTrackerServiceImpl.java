package com.learn.issuetracker.service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.learn.issuetracker.exceptions.IssueNotFoundException;
import com.learn.issuetracker.model.Employee;
import com.learn.issuetracker.model.Issue;
import com.learn.issuetracker.repository.IssueRepository;

/*
 * This class contains functionalities for searching and analyzing Issues data Which is stored in a collection
 * Use JAVA8 STREAMS API to do the analysis
 * 
*/
public class IssueTrackerServiceImpl implements IssueTrackerService {

	/*
	 * CURRENT_DATE contains the date which is considered as todays date for this
	 * application Any logic which uses current date in this application, should
	 * consider this date as current date
	 */
	private static final String CURRENT_DATE = "2019-05-01";

	/*
	 * The issueDao should be used to get the List of Issues, populated from the
	 * file
	 */
	private IssueRepository issueDao;
	private LocalDate today;

	/*
	 * Initialize the member variables Variable today should be initialized with the
	 * value in CURRENT_DATE variable
	 */
	
	public IssueTrackerServiceImpl(IssueRepository issueDao) {
		this.issueDao=issueDao;
		this.today=LocalDate.parse(CURRENT_DATE);
	}

	/*
	 * In all the below methods, the list of issues should be obtained by used
	 * appropriate getter method of issueDao.
	 */
	/*
	 * The below method should return the count of issues which are closed.
	 */
	@Override
	public long getClosedIssueCount() {
		long closedCount=issueDao.getIssues().stream().filter(i-> i.getStatus().equalsIgnoreCase("closed")).count();
		return closedCount;
	}

	/*
	 * The below method should return the Issue details given a issueId. If the
	 * issue is not found, method should throw IssueNotFoundException
	 */

	@Override
	public Issue getIssueById(String issueId) throws IssueNotFoundException {
		Optional<Issue> issueObj=issueDao.getIssues().stream().filter(i->i.getIssueId().equalsIgnoreCase(issueId)).findAny();
		if(issueObj.get()!=null){
			return issueObj.get();
		}
		else{
			throw new IssueNotFoundException();
		}
		
	}

	/*
	 * The below method should return the Employee Assigned to the issue given a
	 * issueId. It should return the employee in an Optional. If the issue is not
	 * assigned to any employee or the issue Id is incorrect the method should
	 * return empty optional
	 */
	@Override
	public Optional<Employee> getIssueAssignedTo(String issueId) {
		Optional<Employee> emptyEmp = Optional.empty();
		if(issueId==null ||issueId.isEmpty()) {
			return emptyEmp;
		}
		List<Issue> issueList = issueDao.getIssues();
		Optional<Employee> optionalEmployee = issueList.stream().filter(p-> p.getIssueId().equalsIgnoreCase(issueId))
		.map(m->m.getAssignedTo()).findFirst();
		if(optionalEmployee.isPresent()) {
			return optionalEmployee;
		}
		else {
			return emptyEmp;
		}
	}

	/*
	 * The below method should return the list of Issues given the status. The
	 * status can contain values OPEN / CLOSED
	 */
	@Override
	public List<Issue> getIssuesByStatus(String status) {
		if(status==null ||status.isEmpty()) {
			return null;
		}
		List<Issue> issueList = issueDao.getIssues();
		issueList = issueList.stream().filter(p->p.getStatus().matches("OPEN/CLOSED"))
		.collect(Collectors.toList());
		return issueList;
	}

	/*
	 * The below method should return a LinkedHashSet containing issueid's of open
	 * issues in the ascending order of expected resolution date
	 */
	@Override
	public Set<String> getOpenIssuesInExpectedResolutionOrder() {
		
		List<Issue> issueList = issueDao.getIssues();
		Set<String> result = issueList.stream()
		.filter(f-> f.getStatus().equalsIgnoreCase("Open"))
		.sorted((ob1,ob2) -> ob1.getExpectedResolutionOn().compareTo(ob2.getExpectedResolutionOn()))
		.map(m-> m.getIssueId())
		.collect(Collectors.toCollection(LinkedHashSet::new));
		return result;
	}

	/*
	 * The below method should return a List of open Issues in the descending order
	 * of Priority and ascending order of expected resolution date within a priority
	 */
	@Override
	public List<Issue> getOpenIssuesOrderedByPriorityAndResolutionDate() {
		List<Issue> issueList = issueDao.getIssues();
		Comparator<Issue> sortByPriorityDesc =(p1,p2) -> p2.getPriority().compareToIgnoreCase(p1.getPriority());
		Comparator<Issue> sortByResolutionDateAsc=(p1,p2) -> p1.getExpectedResolutionOn().compareTo(p2.getExpectedResolutionOn());
		issueList=issueList.stream().filter(p->p.getStatus().equalsIgnoreCase("open"))
		.sorted(sortByPriorityDesc.thenComparing(sortByResolutionDateAsc))
		.collect(Collectors.toList());
		return issueList;
	}

	/*
	 * The below method should return a List of 'unique' employee names who have
	 * issues not closed even after 7 days of Expected Resolution date. Consider the
	 * current date as 2019-05-01
	 */
	@Override
	public List<String> getOpenIssuesDelayedbyEmployees() {
		List<Issue> issueList = issueDao.getIssues();
		//LocalDate currentDate = LocalDate.parse(CURRENT_DATE);
		List<String> list = issueList.stream()
		.filter(p -> ChronoUnit.DAYS.between(p.getExpectedResolutionOn(),today)>7 && p.getStatus().equalsIgnoreCase("open"))
		.map(m->m.getAssignedTo().getName()).distinct()
		.collect(Collectors.toList());
		return list;
		
	}

	/*
	 * The below method should return a map with key as issueId and value as
	 * assigned employee Id. THe Map should contain details of open issues having
	 * HIGH priority
	 */
	@Override
	public Map<String, Integer> getHighPriorityOpenIssueAssignedTo() {
		List<Issue> issueList = issueDao.getIssues();
		Map<String,Integer> map = issueList.stream()
		.filter(p->p.getPriority().equalsIgnoreCase("high") && p.getStatus().equalsIgnoreCase("open"))
		.collect(Collectors.toMap(Issue::getIssueId, v->v.getAssignedTo().getEmplId()));
		return map;
	}

	/*
	 * The below method should return open issues grouped by priority in a map. The
	 * map should have key as issue priority and value as list of open Issues
	 */
	@Override
	public Map<String, List<Issue>> getOpenIssuesGroupedbyPriority() {
		List<Issue> issueList = issueDao.getIssues();
		Map<String,List<Issue>> map= issueList.stream().filter(p->p.getStatus().equalsIgnoreCase("open"))
		.collect(Collectors.groupingBy(Issue::getPriority,Collectors.toList()));
		return map;
	}

	/*
	 * The below method should return count of open issues grouped by priority in a map. 
	 * The map should have key as issue priority and value as count of open issues 
	 */
	@Override
	public Map<String, Long> getOpenIssuesCountGroupedbyPriority() {
		List<Issue> issueList = issueDao.getIssues();
		Map<String,Long> map =issueList.stream()
		.filter(p->p.getStatus().equalsIgnoreCase("open"))
		.collect(Collectors.groupingBy(Issue::getPriority,Collectors.counting()));
		return map;
	}
	
	/*
	 * The below method should provide List of issue id's(open), grouped by location
	 * of the assigned employee. It should return a map with key as location and
	 * value as List of issue Id's of open issues
	 */
	@Override
	public Map<String, List<String>> getOpenIssueIdGroupedbyLocation() {
		List<Issue> issueList = issueDao.getIssues();
		Map<String,List<String>> map = issueList.stream()
		.filter(p->p.getStatus().equalsIgnoreCase("open"))
		.collect(Collectors.groupingBy(k->k.getAssignedTo().getLocation(),Collectors.mapping(Issue::getIssueId ,Collectors.toList())));
		return map;
	}
	
	/*
	 * The below method should provide the number of days, since the issue has been
	 * created, for all high/medium priority open issues. It should return a map
	 * with issueId as key and number of days as value. Consider the current date as
	 * 2019-05-01
	 */
	@Override
	public Map<String, Long> getHighMediumOpenIssueDuration() {
		List<Issue> issueList = issueDao.getIssues();
		Map<String,Long> map = issueList.stream()
		.filter(p->p.getPriority().toLowerCase().matches("high|medium") && p.getStatus().equalsIgnoreCase("open"))
		.collect(Collectors.toMap(Issue::getIssueId, k -> ChronoUnit.DAYS.between(k.getCreatedOn(),today)));
		return map;
		
	}
}
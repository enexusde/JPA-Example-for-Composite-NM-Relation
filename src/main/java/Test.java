import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

public class Test {
	public static void main(final String[] args) throws SQLException {
		Test t = new Test();
		t.work("jdbc:hsqldb:mem:hogwarts");
	}

	public void work(final String url) throws SQLException {
		try (var c = DriverManager.getConnection(url)) {
			try (var sf = new MetadataSources(
					new StandardServiceRegistryBuilder().applySetting("hibernate.connection.url", url)
							.applySetting("hibernate.hbm2ddl.auto", "create").build())
					.addAnnotatedClasses(Teacher.class, Student.class).buildMetadata().getSessionFactoryBuilder()
					.build()) {
				var session = sf.openSession();

				inspect(session, "Albus", "Dumbledore");
				inspect(session, "Argus", "Filch");
			}
		}
	}

	private void inspect(final Session session, final String firstname, final String lastname) {
		var teacher = session.find(Teacher.class, new TeacherId(firstname, lastname));
		System.out.println(
				"Teacher: " + teacher.id.tfirstname() + " " + teacher.id.tlastname() + " (" + teacher.age + ")");
		System.out.println(" - Students: ");
		for (var student : teacher.students) {
			System.out.println(
					"    - " + student.id.sfirstname() + " " + student.id.slastname() + " (" + student.age + ")");
		}
		System.out.println();
	}
}

@Embeddable
record StudentId(String sfirstname, String slastname) {
}

@Entity
class Student {
	@EmbeddedId
	StudentId id;
	@Column
	int age;
	@ManyToMany(mappedBy = "students") // non-owning-side
	Set<Teacher> teachers;
}

@Embeddable
record TeacherId(String tfirstname, String tlastname) {
}

@Entity
class Teacher {
	@EmbeddedId
	TeacherId id;
	@Column
	int age;
	@ManyToMany // owning-side
	@JoinTable(name = "Teacher2Student", //
			joinColumns = { //
					@JoinColumn(name = "tfirstname", referencedColumnName = "tfirstname", nullable = false), //
					@JoinColumn(name = "tlastname", referencedColumnName = "tlastname", nullable = false) //
			}, inverseJoinColumns = { //
					@JoinColumn(name = "sfirstname", referencedColumnName = "sfirstname", nullable = false), //
					@JoinColumn(name = "slastname", referencedColumnName = "slastname", nullable = false) //
			})

	Set<Student> students;

}

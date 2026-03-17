import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Oracle AI Database, Spring-Native',
    Svg: require('@site/static/img/logo.svg').default,
    description: (
      <>
        Start with Oracle AI Database starters for UCP, Oracle Wallet, and
        JDBC-backed Spring Boot apps so connection pooling, secure connectivity, and
        database integration are ready out of the box.
      </>
    ),
  },
  {
    title: 'JSON and 26ai Data Features',
    Svg: require('@site/static/img/logo.svg').default,
    description: (
      <>
        Build against Oracle AI Database JSON collections, JSON data tools, and
        JSON Relational Duality Views to model modern application data without
        giving up relational strength.
      </>
    ),
  },
  {
    title: 'Integrations Across Database and OCI',
    Svg: require('@site/static/img/logo.svg').default,
    description: (
      <>
        Use AQ JMS, OKafka, and the Spring Cloud Stream binder for Oracle
        TxEventQ alongside OCI starters for Autonomous AI Database, Vault,
        Streaming, Queue, Storage, and Generative AI.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}

package com.jobseeker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CompanyRegistry {

    @Data
    @AllArgsConstructor
    public static class CompanyInfo {
        private String name;
        private String slug;
        /**
         * Supported scraping platforms:
         * GREENHOUSE, LEVER
         *
         * Non-scraped platforms:
         * WORKDAY, CAREERS_PAGE, UNKNOWN
         */
        private String platform;
        private int tier;
        private String careerUrl;
    }

    private final Map<String, CompanyInfo> companies = new LinkedHashMap<>();

    public CompanyRegistry() {
        // =========================
        // Tier 1 - Highest Priority
        // =========================
        add("Google", "google", "CAREERS_PAGE", 1, "https://careers.google.com/jobs/results/");
        add("Microsoft", "microsoft", "CAREERS_PAGE", 1, "https://jobs.careers.microsoft.com/global/en/search");
        add("Amazon", "amazon", "CAREERS_PAGE", 1, "https://www.amazon.jobs/en/search");
        add("Atlassian", "atlassian", "CAREERS_PAGE", 1, "https://www.atlassian.com/company/careers/all-jobs");
        add("Salesforce", "salesforce", "CAREERS_PAGE", 1, "https://careers.salesforce.com/en/jobs/");
        add("Uber", "uber", "CAREERS_PAGE", 1, "https://www.uber.com/us/en/careers/list/");
        add("LinkedIn", "linkedin", "GREENHOUSE", 1, "https://www.linkedin.com/jobs/");
        add("Adobe", "adobe", "CAREERS_PAGE", 1, "https://careers.adobe.com/us/en/search-results");
        add("ServiceNow", "ServiceNow", "SMARTRECRUITERS", 3, "https://careers.smartrecruiters.com/ServiceNow");
        add("Intuit", "intuit", "CAREERS_PAGE", 1, "https://jobs.intuit.com/search-jobs");
        add("Twilio", "twilio", "GREENHOUSE", 1, "https://boards.greenhouse.io/twilio");
        add("Datadog", "datadog", "GREENHOUSE", 1, "https://boards.greenhouse.io/datadog");
        add("Snowflake", "snowflake", "CAREERS_PAGE", 1, "https://boards.greenhouse.io/snowflakecomputing");
        add("Stripe", "stripe", "GREENHOUSE", 1, "https://stripe.com/jobs/search");
        add("Nutanix", "nutanix", "CAREERS_PAGE", 1, "https://www.nutanix.com/company/careers/jobs");
        add("VMware", "vmware", "CAREERS_PAGE", 1, "https://jobs.broadcom.com/");
        add("Oracle", "oracle", "CAREERS_PAGE", 1, "https://careers.oracle.com/jobs/");
        add("Cisco", "cisco", "CAREERS_PAGE", 1, "https://jobs.cisco.com/jobs/SearchJobs/");
        add("SAP", "sap", "CAREERS_PAGE", 1, "https://jobs.sap.com/");
        add("IBM", "ibm", "CAREERS_PAGE", 1, "https://www.ibm.com/careers/search");

        // =========================
        // Tier 2 - Realistic Targets
        // =========================
        add("PayPal", "paypal", "CAREERS_PAGE", 1, "https://careers.pypl.com/home/");
        add("Walmart", "walmart", "CAREERS_PAGE", 2, "https://careers.walmart.com/");
        add("Target", "target", "CAREERS_PAGE", 2, "https://corporate.target.com/careers");
        add("American Express", "americanexpress", "CAREERS_PAGE", 2, "https://aexp.eightfold.ai/careers");
        add("JPMorgan", "jpmorgan", "CAREERS_PAGE", 2, "https://careers.jpmorgan.com/global/en/students/programs");
        add("Goldman Sachs", "goldmansachs", "CAREERS_PAGE", 2, "https://www.goldmansachs.com/careers");
        add("Morgan Stanley", "morganstanley", "CAREERS_PAGE", 2, "https://www.morganstanley.com/about-us/careers");
        add("Visa", "Visa", "SMARTRECRUITERS", 3, "https://careers.smartrecruiters.com/Visa");
        add("Mastercard", "mastercard", "CAREERS_PAGE", 2, "https://careers.mastercard.com/us/en/search-results");
        add("Fiserv", "fiserv", "CAREERS_PAGE", 2, "https://www.careers.fiserv.com/");
        add("BlackRock", "blackrock", "CAREERS_PAGE", 2, "https://careers.blackrock.com/");
        add("Fidelity", "fidelity", "CAREERS_PAGE", 2, "https://jobs.fidelity.com/");
        add("BNY Mellon", "bnymellon", "CAREERS_PAGE", 2, "https://jobs.bny.com/");
        add("Thomson Reuters", "thomsonreuters", "CAREERS_PAGE", 2, "https://www.thomsonreuters.com/en/careers.html");
        add("Bloomberg", "bloomberg", "CAREERS_PAGE", 2, "https://www.bloomberg.com/company/careers/working-here/");
        add("FactSet", "factset", "CAREERS_PAGE", 2, "https://www.factset.com/careers");
        add("Verizon", "verizon", "CAREERS_PAGE", 2, "https://mycareer.verizon.com/jobs/");
        add("Dell", "dell", "CAREERS_PAGE", 2, "https://jobs.dell.com/");
        add("HP", "hp", "CAREERS_PAGE", 2, "https://jobs.hp.com/");
        add("Intel", "intel", "CAREERS_PAGE", 1, "https://jobs.intel.com/");

        // =========================
        // Tier 3 - Indian Product Companies
        // =========================
        add("Zoho", "zoho", "CAREERS_PAGE", 3, "https://www.zoho.com/careers/jobdetails/");
        add("Freshworks", "freshworks", "CAREERS_PAGE", 3, "https://www.freshworks.com/company/careers/");
        add("Postman", "postman", "GREENHOUSE", 3, "https://boards.greenhouse.io/postman");
        add("BrowserStack", "browserstack", "CAREERS_PAGE", 3, "https://www.browserstack.com/careers/jobs");
        add("Razorpay", "razorpaysoftwareprivatelimited", "GREENHOUSE", 2, "https://job-boards.greenhouse.io/razorpaysoftwareprivatelimited");
        add("PhonePe", "phonepe", "GREENHOUSE", 2, "https://job-boards.greenhouse.io/phonepe");
        add("CRED", "cred", "LEVER", 2, "https://jobs.lever.co/cred");
        add("Groww", "groww", "GREENHOUSE", 2, "https://groww.in/about-us/careers");
        add("Zerodha", "zerodha", "CAREERS_PAGE", 2, "https://zerodha.com/careers/");
        add("Juspay", "juspay", "CAREERS_PAGE", 2, "https://juspay.io/careers");
        add("Meesho", "meesho", "LEVER", 3, "https://jobs.lever.co/meesho");
        add("Flipkart", "flipkart", "CAREERS_PAGE", 3, "https://www.flipkartcareers.com/");
        add("Myntra", "myntra", "CAREERS_PAGE", 3, "https://www.myntra.com/careers");
        add("Swiggy", "swiggy", "CAREERS_PAGE", 3, "https://careers.swiggy.com/");
        add("Zomato", "zomato", "CAREERS_PAGE", 3, "https://www.zomato.com/careers");
        add("Dream11", "dream11", "CAREERS_PAGE", 3, "https://careers.dreamsports.group/");
        add("Upstox", "upstox", "CAREERS_PAGE", 2, "https://upstox.com/careers/");
        add("CoinSwitch", "coinswitch", "CAREERS_PAGE", 2, "https://coinswitch.co/careers/");
        add("Slice", "slice", "GREENHOUSE", 3, "https://www.sliceit.com/careers");
        add("Navi", "navi", "CAREERS_PAGE", 2, "https://navi.com/careers");

        // =========================
        // Tier 4 - AI & SaaS
        // =========================
        add("Glean", "gleanwork", "GREENHOUSE", 4, "https://job-boards.greenhouse.io/gleanwork");
        add("Cohere", "cohere", "ASHBY", 3, "https://jobs.ashbyhq.com/cohere");
        add("DataRobot", "datarobot", "CAREERS_PAGE", 4, "https://www.datarobot.com/careers/");
        add("Scale AI", "scaleai", "GREENHOUSE", 4, "https://job-boards.greenhouse.io/scaleai");
        add("Weights & Biases", "wandb", "CAREERS_PAGE", 4, "https://jobs.lever.co/wandb");
        add("Pinecone", "pinecone", "CAREERS_PAGE", 4, "https://jobs.lever.co/pinecone");
        add("LangChain", "langchain", "CAREERS_PAGE", 4, "https://www.langchain.com/careers");
        add("Elastic", "elastic", "GREENHOUSE", 1, "https://boards.greenhouse.io/elastic");
        add("Confluent", "confluent", "CAREERS_PAGE", 1, "https://www.confluent.io/careers/");
        add("MongoDB", "mongodb", "GREENHOUSE", 1, "https://www.mongodb.com/careers/jobs");
        add("Apple", "apple", "CAREERS_PAGE", 1, "https://jobs.apple.com/");
        add("Meta", "meta", "CAREERS_PAGE", 1, "https://www.metacareers.com/jobs/");
        add("Netflix", "netflix", "CAREERS_PAGE", 1, "https://jobs.netflix.com/");
        add("NVIDIA", "nvidia", "CAREERS_PAGE", 1, "https://www.nvidia.com/en-us/about-nvidia/careers/");
        add("Qualcomm", "qualcomm", "CAREERS_PAGE", 1, "https://careers.qualcomm.com/careers");
        add("AMD", "amd", "CAREERS_PAGE", 1, "https://careers.amd.com/");
        add("Broadcom", "broadcom", "CAREERS_PAGE", 1, "https://jobs.broadcom.com/");
        add("Airbnb", "airbnb", "GREENHOUSE", 1, "https://careers.airbnb.com/");
        add("Dropbox", "dropbox", "GREENHOUSE", 1, "https://jobs.dropbox.com/");
        add("Box", "box", "CAREERS_PAGE", 1, "https://www.box.com/careers");
        add("Zoom", "zoom", "CAREERS_PAGE", 1, "https://careers.zoom.us/");
        add("Slack", "slack", "CAREERS_PAGE", 1, "https://slack.com/intl/en-in/careers");
        add("GitHub", "github", "CAREERS_PAGE", 1, "https://www.github.careers/careers-home/");
        add("GitLab", "gitlab", "GREENHOUSE", 1, "https://about.gitlab.com/jobs/");
        add("Cloudflare", "cloudflare", "GREENHOUSE", 1, "https://www.cloudflare.com/careers/jobs/");
        add("Akamai", "akamai", "CAREERS_PAGE", 1, "https://www.akamai.com/careers");
        add("Red Hat", "redhat", "CAREERS_PAGE", 1, "https://www.redhat.com/en/jobs");
        add("Palantir", "palantir", "LEVER", 1, "https://www.palantir.com/careers/");
        add("Square / Block", "squareblock", "CAREERS_PAGE", 1, "https://block.xyz/careers/jobs");
        add("Shopify", "shopify", "CAREERS_PAGE", 1, "https://www.shopify.com/careers");
        add("Roblox", "roblox", "GREENHOUSE", 1, "https://careers.roblox.com/jobs");
        add("Electronic Arts", "electronicarts", "CAREERS_PAGE", 1, "https://www.ea.com/careers");
        add("Autodesk", "autodesk", "CAREERS_PAGE", 1, "https://www.autodesk.com/careers/job-search");
        add("Workday", "workday", "CAREERS_PAGE", 1, "https://workday.wd5.myworkdayjobs.com/Workday");
        add("Okta", "okta", "GREENHOUSE", 1, "https://www.okta.com/company/careers/");
        add("Splunk", "splunk", "CAREERS_PAGE", 1, "https://www.splunk.com/en_us/careers/search-jobs.html");
        add("JPMorgan Chase", "jpmorganchase", "CAREERS_PAGE", 2, "https://careers.jpmorgan.com/global/en/home");
        add("Wells Fargo", "wellsfargo", "CAREERS_PAGE", 2, "https://www.wellsfargojobs.com/");
        add("Capital One", "capitalone", "CAREERS_PAGE", 2, "https://www.capitalonecareers.com/");
        add("Nasdaq", "nasdaq", "CAREERS_PAGE", 2, "https://www.nasdaq.com/about/careers");
        add("DTCC", "dtcc", "CAREERS_PAGE", 2, "https://www.dtcc.com/careers");
        add("Coinbase", "coinbase", "GREENHOUSE", 2, "https://www.coinbase.com/careers");
        add("Robinhood", "robinhood", "GREENHOUSE", 2, "https://careers.robinhood.com/");
        add("Plaid", "plaid", "CAREERS_PAGE", 2, "https://plaid.com/careers/");
        add("Brex", "brex", "GREENHOUSE", 2, "https://www.brex.com/careers");
        add("Ramp", "ramp", "ASHBY", 2, "https://jobs.ashbyhq.com/ramp");
        add("Affirm", "affirm", "GREENHOUSE", 2, "https://www.affirm.com/careers");
        add("Wise", "Wise", "SMARTRECRUITERS", 3, "https://careers.smartrecruiters.com/Wise");
        add("Revolut", "revolut", "CAREERS_PAGE", 2, "https://www.revolut.com/careers/");
        add("Adyen", "adyen", "GREENHOUSE", 2, "https://careers.adyen.com/");
        add("Paytm", "paytm", "LEVER", 2, "https://paytm.com/careers/");
        add("Pine Labs", "pine", "GREENHOUSE", 2, "https://www.pinelabs.com/careers");
        add("MobiKwik", "mobikwik", "CAREERS_PAGE", 2, "https://www.mobikwik.com/careers");
        add("Jar", "jar", "CAREERS_PAGE", 2, "https://www.myjar.app/careers");
        add("Fi Money", "fimoney", "CAREERS_PAGE", 2, "https://fi.money/careers");
        add("Dream Sports", "dreamsports", "LEVER", 3, "https://careers.dreamsports.group/");
        add("Gameskraft", "gameskraft", "CAREERS_PAGE", 3, "https://www.gameskraft.com/careers");
        add("MPL", "mpl", "CAREERS_PAGE", 3, "https://www.mpl.live/careers");
        add("Ola", "ola", "CAREERS_PAGE", 3, "https://www.olacabs.com/careers");
        add("Ola Electric", "olaelectric", "CAREERS_PAGE", 3, "https://olaelectric.com/careers");
        add("Ather Energy", "atherenergy", "CAREERS_PAGE", 3, "https://www.atherenergy.com/careers");
        add("Urban Company", "urbancompany", "CAREERS_PAGE", 3, "https://www.urbancompany.com/careers");
        add("NoBroker", "nobroker", "CAREERS_PAGE", 3, "https://www.nobroker.in/careers");
        add("Lenskart", "lenskart", "CAREERS_PAGE", 3, "https://www.lenskart.com/careers");
        add("Nykaa", "nykaa", "CAREERS_PAGE", 3, "https://www.nykaa.com/careers");
        add("Tata Neu", "tataneu", "CAREERS_PAGE", 3, "https://www.tatadigital.com/careers");
        add("Jio Platforms", "jioplatforms", "CAREERS_PAGE", 3, "https://careers.jio.com/");
        add("Disney+ Hotstar", "disneyhotstar", "CAREERS_PAGE", 3, "https://careers.hotstar.com/");
        add("ShareChat", "sharechat", "CAREERS_PAGE", 3, "https://sharechat.com/careers");
        add("Dailyhunt / VerSe", "dailyhuntverse", "CAREERS_PAGE", 3, "https://www.verse.in/careers");
        add("InMobi", "inmobi", "GREENHOUSE", 3, "https://www.inmobi.com/company/careers");
        add("Glance", "glance", "GREENHOUSE", 3, "https://glance.com/careers");
        add("Chargebee", "chargebee", "CAREERS_PAGE", 3, "https://www.chargebee.com/careers/");
        add("Druva", "druva", "GREENHOUSE", 3, "https://www.druva.com/about/careers/");
        add("Icertis", "icertis", "CAREERS_PAGE", 3, "https://www.icertis.com/company/careers/");
        add("Whatfix", "whatfix", "CAREERS_PAGE", 3, "https://whatfix.com/careers/");
        add("Tekion", "tekion", "GREENHOUSE", 3, "https://tekion.com/careers/");
        add("Observe.AI", "observeai", "GREENHOUSE", 3, "https://www.observe.ai/careers");
        add("Innovaccer", "innovaccer", "CAREERS_PAGE", 3, "https://innovaccer.com/careers");
        add("MindTickle", "mindtickle", "LEVER", 3, "https://www.mindtickle.com/careers/");
        add("LambdaTest", "lambdatest", "CAREERS_PAGE", 3, "https://www.lambdatest.com/career");
        add("Hasura", "hasura", "CAREERS_PAGE", 3, "https://hasura.io/careers/");
        add("Zeta", "zeta", "LEVER", 3, "https://www.zeta.tech/careers/");
        add("Setu", "setu", "CAREERS_PAGE", 3, "https://setu.co/careers");
        add("Khatabook", "khatabook", "CAREERS_PAGE", 3, "https://khatabook.com/careers");
        add("Open Financial Technologies", "openfinancialtechnologies", "CAREERS_PAGE", 3, "https://open.money/careers");
        add("Cashfree Payments", "cashfreepayments", "CAREERS_PAGE", 3, "https://www.cashfree.com/careers/");
        add("Smallcase", "smallcase", "CAREERS_PAGE", 3, "https://www.smallcase.com/careers");
        add("ClearTax", "cleartax", "CAREERS_PAGE", 3, "https://cleartax.in/careers");
        add("KreditBee", "kreditbee", "CAREERS_PAGE", 3, "https://www.kreditbee.in/careers");
        add("Simpl", "simpl", "CAREERS_PAGE", 3, "https://getsimpl.com/careers");
        add("Fampay", "fampay", "LEVER", 3, "https://www.famapp.in/careers");
        add("SuperMoney", "supermoney", "CAREERS_PAGE", 3, "https://super.money/careers");
        add("OpenAI", "openai", "ASHBY", 4, "https://jobs.ashbyhq.com/openai");
        add("Anthropic", "anthropic", "GREENHOUSE", 4, "https://www.anthropic.com/careers");
        add("Mistral AI", "mistral", "LEVER", 4, "https://mistral.ai/careers/");
        add("Perplexity AI", "perplexityai", "CAREERS_PAGE", 4, "https://www.perplexity.ai/careers");
        add("Anysphere / Cursor", "anyspherecursor", "CAREERS_PAGE", 4, "https://www.cursor.com/careers");
        add("Hugging Face", "huggingface", "CAREERS_PAGE", 4, "https://apply.workable.com/huggingface/");
        add("Databricks", "databricks", "GREENHOUSE", 4, "https://www.databricks.com/company/careers/open-positions");
        add("Redis", "redis", "CAREERS_PAGE", 4, "https://redis.io/careers/");
        add("Cockroach Labs", "cockroachlabs", "GREENHOUSE", 4, "https://www.cockroachlabs.com/careers/");
        add("PlanetScale", "planetscale", "GREENHOUSE", 4, "https://planetscale.com/careers");
        add("Supabase", "supabase", "ASHBY", 3, "https://jobs.ashbyhq.com/supabase");
        add("Vercel", "vercel", "GREENHOUSE", 4, "https://vercel.com/careers");
        add("Netlify", "netlify", "GREENHOUSE", 4, "https://www.netlify.com/careers/");
        add("Render", "render", "CAREERS_PAGE", 4, "https://render.com/careers");
        add("Railway", "railway", "CAREERS_PAGE", 4, "https://railway.com/careers");
        add("Temporal", "temporal", "ASHBY", 3, "https://jobs.ashbyhq.com/temporal");
        add("Tailscale", "tailscale", "GREENHOUSE", 4, "https://tailscale.com/careers");
        add("HashiCorp", "hashicorp", "CAREERS_PAGE", 4, "https://www.hashicorp.com/careers");
        add("Snyk", "snyk", "CAREERS_PAGE", 4, "https://snyk.io/careers/");
        add("Wiz", "wiz", "CAREERS_PAGE", 4, "https://www.wiz.io/careers");
        add("CrowdStrike", "crowdstrike", "CAREERS_PAGE", 4, "https://www.crowdstrike.com/careers/");
        add("Palo Alto Networks", "paloaltonetworks", "CAREERS_PAGE", 4, "https://jobs.paloaltonetworks.com/");
        add("Zscaler", "zscaler", "GREENHOUSE", 4, "https://www.zscaler.com/careers");
        add("SentinelOne", "sentinelone", "CAREERS_PAGE", 4, "https://www.sentinelone.com/careers/");
        add("Auth0", "auth0", "CAREERS_PAGE", 4, "https://www.okta.com/company/careers/");
        add("1Password", "1password", "CAREERS_PAGE", 4, "https://1password.com/careers");
        add("Sentry", "sentry", "CAREERS_PAGE", 4, "https://sentry.io/careers/");
        add("Grafana Labs", "grafanalabs", "GREENHOUSE", 4, "https://grafana.com/about/careers/");
        add("New Relic", "newrelic", "GREENHOUSE", 4, "https://newrelic.com/about/careers");
        add("PagerDuty", "pagerduty", "GREENHOUSE", 4, "https://careers.pagerduty.com/");
        add("CircleCI", "circleci", "GREENHOUSE", 4, "https://circleci.com/careers/");
        add("Docker", "docker", "ASHBY", 3, "https://jobs.ashbyhq.com/docker");
        add("Kong", "kong", "ASHBY", 3, "https://jobs.ashbyhq.com/kong");
        add("PostHog", "posthog", "ASHBY", 4, "https://jobs.ashbyhq.com/posthog");
        add("Linear", "linear", "ASHBY", 4, "https://jobs.ashbyhq.com/linear");
        add("Notion", "notion", "ASHBY", 4, "https://jobs.ashbyhq.com/notion");
        add("Airtable", "airtable", "GREENHOUSE", 4, "https://www.airtable.com/careers");
        add("Asana", "asana", "GREENHOUSE", 4, "https://asana.com/jobs");
        add("Figma", "figma", "GREENHOUSE", 4, "https://www.figma.com/careers/");
        add("Canva", "canva", "CAREERS_PAGE", 4, "https://www.canva.com/careers/");
        add("Miro", "miro", "CAREERS_PAGE", 4, "https://miro.com/careers/");
        add("Grammarly", "grammarly", "CAREERS_PAGE", 4, "https://www.grammarly.com/jobs");
        add("Duolingo", "duolingo", "GREENHOUSE", 4, "https://careers.duolingo.com/");
        add("Khan Academy", "khanacademy", "GREENHOUSE", 4, "https://www.khanacademy.org/careers");
        add("Coursera", "coursera", "GREENHOUSE", 4, "https://about.coursera.org/careers");
        add("Udemy", "udemy", "GREENHOUSE", 4, "https://about.udemy.com/careers/");
        add("Pluralsight", "pluralsight", "CAREERS_PAGE", 4, "https://www.pluralsight.com/careers");
        add("Notion Labs", "notionlabs", "CAREERS_PAGE", 4, "https://www.notion.com/careers");
        add("HubSpot", "hubspot", "CAREERS_PAGE", 4, "https://www.hubspot.com/careers");
        add("Zendesk", "zendesk", "CAREERS_PAGE", 4, "https://www.zendesk.com/company/careers/");
        add("Intercom", "intercom", "GREENHOUSE", 4, "https://www.intercom.com/careers");
        add("Freshpaint", "freshpaint", "CAREERS_PAGE", 4, "https://www.freshpaint.io/careers");
        add("Amplitude", "amplitude", "GREENHOUSE", 4, "https://amplitude.com/careers");
        add("Mixpanel", "mixpanel", "GREENHOUSE", 4, "https://mixpanel.com/careers/");
        add("Segment", "segment", "CAREERS_PAGE", 4, "https://www.twilio.com/en-us/company/jobs");
        add("Braze", "braze", "GREENHOUSE", 4, "https://www.braze.com/careers");
        add("Iterable", "iterable", "GREENHOUSE", 4, "https://iterable.com/careers/");
        add("Klaviyo", "klaviyo", "GREENHOUSE", 4, "https://www.klaviyo.com/careers");
        add("ServiceTitan", "servicetitan", "CAREERS_PAGE", 4, "https://www.servicetitan.com/careers");
        add("Rippling", "rippling", "CAREERS_PAGE", 4, "https://www.rippling.com/careers");
        add("Replit", "replit", "ASHBY", 4, "https://jobs.ashbyhq.com/replit");
        add("Harvey", "harvey", "ASHBY", 4, "https://jobs.ashbyhq.com/harvey");
        add("ElevenLabs", "elevenlabs", "ASHBY", 4, "https://jobs.ashbyhq.com/elevenlabs");
        add("Airwallex", "airwallex", "ASHBY", 4, "https://jobs.ashbyhq.com/airwallex");
        add("Zapier", "zapier", "ASHBY", 3, "https://jobs.ashbyhq.com/zapier");
        add("Anysphere", "cursor", "ASHBY", 3, "https://jobs.ashbyhq.com/cursor");
        add("Vanta", "vanta", "ASHBY", 3, "https://jobs.ashbyhq.com/vanta");
        add("Turing", "turing", "GREENHOUSE", 3, "https://boards.greenhouse.io/turing");
        add("Nium", "nium", "LEVER", 2, "https://jobs.lever.co/nium");
        add("Netomi", "netomi", "LEVER", 3, "https://jobs.lever.co/netomi");
        add("Lightspeed", "lightspeed", "ASHBY", 3, "https://jobs.ashbyhq.com/lightspeed");
        add("ClickHouse", "clickhouse", "GREENHOUSE", 3, "https://boards.greenhouse.io/clickhouse");
        add("Starburst", "starburst", "GREENHOUSE", 3, "https://boards.greenhouse.io/starburst");
        add("Crusoe", "crusoe", "ASHBY", 3, "https://jobs.ashbyhq.com/crusoe");
        add("Bosch Group", "BoschGroup", "CAREERS_PAGE", 4, "https://careers.smartrecruiters.com/BoschGroup");
        add("Nexthink", "Nexthink", "SMARTRECRUITERS", 3, "https://careers.smartrecruiters.com/Nexthink");
    }

    private void add(String name, String slug, String platform, int tier, String careerUrl) {
        companies.put(name.toLowerCase(), new CompanyInfo(name, slug, platform, tier, careerUrl));
    }

    public List<CompanyInfo> getAllCompanies() {
        return new ArrayList<>(companies.values());
    }

    public List<CompanyInfo> getScrapableCompanies() {
        return companies.values().stream()
                .filter(c -> c.getPlatform().equals("GREENHOUSE") || c.getPlatform().equals("LEVER"))
                .toList();
    }

    public List<CompanyInfo> getByTier(int tier) {
        return companies.values().stream().filter(c -> c.getTier() == tier).toList();
    }

    public Optional<CompanyInfo> getCompany(String name) {
        return Optional.ofNullable(companies.get(name.toLowerCase()));
    }

    public int totalCompanies() {
        return companies.size();
    }
}

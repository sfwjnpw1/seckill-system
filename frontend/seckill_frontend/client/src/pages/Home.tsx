import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { APP_LOGO, APP_TITLE } from "@/const";
import { Streamdown } from 'streamdown';

/**
 * All content in this page are only for example, replace with your own feature implementation
 * When building pages, remember your instructions in Frontend Best Practices, Design Guide and Common Pitfalls
 */
export default function Home() {
  // If theme is switchable in App.tsx, we can implement theme toggling like this:
  // const { theme, toggleTheme } = useTheme();

  // Use APP_LOGO (as image src) and APP_TITLE if needed

  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50">
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Seckill System</h1>
          <div className="flex gap-4">
            <Button variant="outline" onClick={() => window.location.href = "/login"}>
              Login
            </Button>
            <Button onClick={() => window.location.href = "/register"}>
              Register
            </Button>
          </div>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center px-4">
        <div className="text-center max-w-3xl">
          <h1 className="text-5xl font-bold text-gray-900 mb-6">
            Welcome to Seckill System
          </h1>
          <p className="text-xl text-gray-600 mb-8">
            Experience lightning-fast flash sales with our high-performance seckill platform.
            Built with Spring Cloud microservices architecture.
          </p>
          <div className="flex gap-4 justify-center">
            <Button size="lg" onClick={() => window.location.href = "/products"}>
              View Seckill Activities
            </Button>
            <Button size="lg" variant="outline" onClick={() => window.location.href = "/login"}>
              Get Started
            </Button>
          </div>
        </div>
      </main>

      <footer className="bg-white border-t py-6">
        <div className="container mx-auto px-4 text-center text-gray-600">
          <p>&copy; 2024 Seckill System. Built with Spring Cloud + Vue 3.</p>
        </div>
      </footer>
    </div>
  );
}
